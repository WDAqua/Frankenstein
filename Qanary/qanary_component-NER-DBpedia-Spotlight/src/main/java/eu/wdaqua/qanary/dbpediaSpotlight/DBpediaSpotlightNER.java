package eu.wdaqua.qanary.dbpediaSpotlight;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.wdaqua.qanary.commons.QanaryMessage;
import eu.wdaqua.qanary.commons.QanaryQuestion;
import eu.wdaqua.qanary.commons.QanaryUtils;
import eu.wdaqua.qanary.component.QanaryComponent;

/**
 * represents a wrapper of the DBpedia Spotlight tool used here as a spotter
 *
 * @author Kuldeep Singh
 */

@Component
public class DBpediaSpotlightNER extends QanaryComponent {
	private static final Logger logger = LoggerFactory.getLogger(DBpediaSpotlightNER.class);

	/**
	 * default processor of a QanaryMessage
	 */

	private List<String> usingXml(String urladd) {
		List<String> retLst = new ArrayList<String>();
		try {

			URL url = new URL(urladd);
			URLConnection urlConnection = url.openConnection();
			HttpURLConnection connection = null;
			connection = (HttpURLConnection) urlConnection;

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(connection.getInputStream());

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("surfaceForm");

			boolean flg = true;
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					String text = eElement.getAttribute("name");
					String offset = eElement.getAttribute("offset");

					String startEnd = Integer.parseInt(offset) + "," + (text.length() + Integer.parseInt(offset));
					retLst.add(startEnd);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retLst;
	}

	public QanaryMessage process(QanaryMessage myQanaryMessage) {
		long startTime = System.currentTimeMillis();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		logger.info("Qanary Message: {}", myQanaryMessage);
		ArrayList<Selection> selections = new ArrayList<Selection>();
		try {

			// STEP1: Retrieve the named graph and the endpoint

			// the class QanaryUtils provides some helpers for standard tasks
			    QanaryUtils myQanaryUtils = this.getUtils(myQanaryMessage);
			   QanaryQuestion<String> myQanaryQuestion = this.getQanaryQuestion(myQanaryMessage);

			// question string is required as input for the service call
			 String myQuestion = myQanaryQuestion.getTextualRepresentation();
			//String myQuestion = "Who is the wife of Barack Obama ?";
			logger.info("Question: {}", myQuestion);

			// String uriQuestion="http://wdaqua.eu/dummy";
			// String question="Brooklyn Bridge was designed by Alfred";

			// STEP3: Pass the information to the component and execute it
			// logger.info("apply commons alignment on outgraph");

			String madeUrlFromInput = "http://model.dbpedia-spotlight.org/en/annotate?text=";
			/*
			 * String qns[] = input.split(" "); String append = String.join("%20", qns);
			 */
			try {
				logger.info("Input is: {}", myQuestion);
				madeUrlFromInput += URLEncoder.encode(myQuestion, "UTF-8");
				HttpClient httpclient = HttpClients.createDefault();
				HttpGet httpget = new HttpGet(madeUrlFromInput);
				httpget.addHeader("Accept", "application/json");
				HttpResponse response = httpclient.execute(httpget);
				try {

					HttpEntity entity = response.getEntity();
					if (entity != null) {
						InputStream instream = entity.getContent();
						String text = IOUtils.toString(instream, StandardCharsets.UTF_8.name());
						JSONObject response2 = new JSONObject(text);
						logger.info("response2: {}", response2);
						if (response2.has("Resources")) {
							JSONArray jsonArray = (JSONArray) response2.get("Resources");
							for (int i = 0; i < jsonArray.length(); i++) {

								JSONObject explrObj2 = (JSONObject) jsonArray.get(i);
								int begin = explrObj2.getInt("@offset");
								String endString = (String) explrObj2.get("@surfaceForm");
								int end = begin + endString.length();
								logger.info("Question: {}", explrObj2);
								logger.info("begin: {}", begin);
								logger.info("end: {}", end);
								Selection s = new Selection();
								s.begin = begin;
								s.end = end;
								selections.add(s);
							}
						}
					}

				}

				catch (ClientProtocolException e) {
					logger.info("Exception: {}", e);
					// TODO Auto-generated catch block
				} catch (IOException e1) {
					logger.info("Except: {}", e1);
					// TODO Auto-generated catch block
				}
				// +"&executeSparqlQuery=on&relationExtractorType=Semantic";
			} catch (Exception e) {
				e.printStackTrace();
				logger.warn(e.getMessage());
			}

			// DBpediaSpotlightNER qaw = new DBpediaSpotlightNER();

			// List<String> stEn = new ArrayList<String>();
			// stEn = qaw.getResults(myQuestion);
			// int cnt = 0;
			// ArrayList<Selection> selections = new ArrayList<Selection>();
			// for (String str : stEn) {
			// Selection s1 = new Selection();
			// String str1[] = str.split(",");
			// s1.begin = Integer.parseInt(str1[0]);
			// s1.end = Integer.parseInt(str1[1]);
			// selections.add(s1);
			// }

			// STEP4: Push the result of the component to the triplestore

			for (Selection s : selections) {
				String sparql = "prefix qa: <http://www.wdaqua.eu/qa#> "
						+ "prefix oa: <http://www.w3.org/ns/openannotation/core/> "
						+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "INSERT { "
					    + "GRAPH <" + myQanaryQuestion.getOutGraph() + "> { "
						+ "  ?a a qa:AnnotationOfSpotInstance . " + "  ?a oa:hasTarget [ "
						+ "           a    oa:SpecificResource; "
					    + " oa:hasSource <" + myQanaryQuestion.getUri() + ">; "
						+ "           oa:hasSelector  [ " + "                    a oa:TextPositionSelector ; "
						+ "                    oa:start \"" + s.begin + "\"^^xsd:nonNegativeInteger ; "
						+ "                    oa:end  \"" + s.end + "\"^^xsd:nonNegativeInteger  " + "           ] "
						+ "  ] ; " + "     oa:annotatedBy <http://model.dbpedia-spotlight.org/en/annotatet> ; "
						+ "	    oa:AnnotatedAt ?time  " + "}} " + "WHERE { " + "BIND (IRI(str(RAND())) AS ?a) ."
						+ "BIND (now() as ?time) " + "}";
				myQanaryUtils.updateTripleStore(sparql, myQanaryQuestion.getEndpoint().toString());
			}
			long estimatedTime = System.currentTimeMillis() - startTime;
			logger.info("Time {}", estimatedTime);

		} catch (Exception e) {// MalformedURLException e) {
			e.printStackTrace();
		}

		return myQanaryMessage;
	}

	class Selection {
		public int begin;
		public int end;
	}

}
