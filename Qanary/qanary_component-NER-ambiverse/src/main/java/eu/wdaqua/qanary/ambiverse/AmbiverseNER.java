package eu.wdaqua.qanary.ambiverse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;


import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.wdaqua.qanary.commons.QanaryMessage;
import eu.wdaqua.qanary.commons.QanaryQuestion;
import eu.wdaqua.qanary.commons.QanaryUtils;
import eu.wdaqua.qanary.component.QanaryComponent;

@Component
/**
 * This component connected automatically to the Qanary pipeline.
 * The Qanary pipeline endpoint defined in application.properties (spring.boot.admin.url)
 * @see <a href="https://github.com/WDAqua/Qanary/wiki/How-do-I-integrate-a-new-component-in-Qanary%3F" target="_top">Github wiki howto</a>
 */
public class AmbiverseNER extends QanaryComponent {
	private static final Logger logger = LoggerFactory.getLogger(AmbiverseNER.class);
	private final String CLIENT_ID  = "5e15e933";
	private final String CLIENT_SECRET = "a09256c925adc9e2279435038df9d55e";


	/**
	 * implement this method encapsulating the functionality of your Qanary
	 * component
	 * @throws Exception 
	 */
	@Override
	public QanaryMessage process(QanaryMessage myQanaryMessage) throws Exception {
		logger.info("process: {}", myQanaryMessage);
		QanaryUtils myQanaryUtils = this.getUtils(myQanaryMessage);
		QanaryQuestion<String> myQanaryQuestion = new QanaryQuestion(myQanaryMessage);
		String myQuestion = myQanaryQuestion.getTextualRepresentation();
		//String myQuestion = "Who is the wife of Barak Obama ?";
		ArrayList<Selection> selections = new ArrayList<Selection>();
		logger.info("Question {}", myQuestion);
		String thePath = URLEncoder.encode(myQuestion, "UTF-8");
		String jsonThePath = (new JSONObject()).put("text", thePath).toString();

		logger.info("JsonPath {}", jsonThePath);

		String urlAccessToken = "https://api.ambiverse.com/oauth/token";
		String urlEntityLinkService = "https://api.ambiverse.com/v1/entitylinking/analyze";
		String[] accessTokenCmd = {"curl", "-X", "POST", "-H", 
				"Content-Type: application/x-www-form-urlencoded",
				"-d", "grant_type=client_credentials",
				"-d", "client_id="+CLIENT_ID,
				"-d", "client_secret="+CLIENT_SECRET, urlAccessToken};

		try {
			ProcessBuilder process = new ProcessBuilder(accessTokenCmd);
			Process p = process.start();
			InputStream instream = p.getInputStream();
			String text = IOUtils.toString(instream, StandardCharsets.UTF_8.name());
			logger.info("AccessTokenInfo: {}", text);
			JSONObject accessTokenInfo = new JSONObject(text);
			logger.info("AccessTokenInfo: {}", accessTokenInfo);
			String access_token = accessTokenInfo.getString("access_token");
			logger.info("AccessToken:  {}", access_token);

			if(access_token != null){

				String[] entityLinkCmd = {"curl", "--compressed", "-X", "POST", "-H", "Content-Type: application/json", 
						"-H", "Accept: application/json", "-H", 
						"Authorization:"+access_token,  "-d", jsonThePath, urlEntityLinkService };


				logger.info("EntityLinkCmd: {}",Arrays.toString(entityLinkCmd));
				ProcessBuilder processEL = new ProcessBuilder(entityLinkCmd);
				logger.info("ProcessEL: {}", processEL.command());
				Process pEL = processEL.start();

				logger.error("Process PEL: {}",IOUtils.toString(pEL.getErrorStream()));
				InputStream instreamEL = pEL.getInputStream();
				String textEL = IOUtils.toString(instreamEL, StandardCharsets.UTF_8.name());
				;
				JSONObject response = new JSONObject(textEL);
				logger.info("response: {}",response.toString());
				JSONArray jsonArrayEL = response.getJSONArray("matches");
				logger.info("EntityLinkInfoArray: {}",jsonArrayEL.toString());

				for (int j = 0; j < jsonArrayEL.length(); j++) {
					JSONObject explrObjectEL = jsonArrayEL.getJSONObject(j);
					logger.info("EntityJsonObject:{}", explrObjectEL.toString());
					int begin = (int) explrObjectEL.get("charOffset");
					int end = begin + (int) explrObjectEL.get("charLength") - 1;
					logger.info("Question: {}", begin);
					logger.info("Question: {}", end);
					Selection s = new Selection();
					s.begin = begin;
					s.end = end;
					selections.add(s);
				}
			}
			else {

				logger.error("Access_Token: {}", "Access token can not be accessed");

			}

		} catch (JSONException e) {
			logger.error("Except: {}", e);

		}catch (IOException e) {
			logger.error("Except: {}", e);
			// TODO Auto-generated catch block
		}
		catch (Exception e) {
			logger.error("Except: {}", e);
			// TODO Auto-generated catch block
		}
		logger.info("store data in graph {}", myQanaryMessage.getValues().get(myQanaryMessage.getEndpoint()));
		// TODO: insert data in QanaryMessage.outgraph

		logger.info("apply vocabulary alignment on outgraph");
		// TODO: implement this (custom for every component)
		for (Selection s : selections) {
			String sparql = "prefix qa: <http://www.wdaqua.eu/qa#> "
					+ "prefix oa: <http://www.w3.org/ns/openannotation/core/> "
					+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "INSERT { " + "GRAPH <" + myQanaryMessage.getOutGraph() + "> { "
					+ "  ?a a qa:AnnotationOfSpotInstance . " + "  ?a oa:hasTarget [ "
					+ "           a    oa:SpecificResource; " + "           oa:hasSource    <" + myQanaryQuestion.getUri() + ">; "
					+ "           oa:hasSelector  [ " + "                    a oa:TextPositionSelector ; "
					+ "                    oa:start \"" + s.begin + "\"^^xsd:nonNegativeInteger ; "
					+ "                    oa:end  \"" + s.end + "\"^^xsd:nonNegativeInteger  " + "           ] "
					+ "  ] ; " + "     oa:annotatedBy <http://ambiverseNER.com> ; "
					+ "	    oa:AnnotatedAt ?time  " + "}} " + "WHERE { " + "BIND (IRI(str(RAND())) AS ?a) ."
					+ "BIND (now() as ?time) " + "}";
			myQanaryUtils.updateTripleStore(sparql);
		}
		return myQanaryMessage;

	}
	class Selection {
		public int begin;
		public int end;
	}
}
