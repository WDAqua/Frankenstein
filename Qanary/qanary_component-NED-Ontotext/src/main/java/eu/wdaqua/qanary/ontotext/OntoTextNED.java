package eu.wdaqua.qanary.ontotext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import eu.wdaqua.qanary.commons.QanaryExceptionNoOrMultipleQuestions;
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
public class OntoTextNED extends QanaryComponent {
	private static final Logger logger = LoggerFactory.getLogger(OntoTextNED.class);

	/**
	 * implement this method encapsulating the functionality of your Qanary
	 * component
	 * @throws Exception 
	 */
	@Override
	public QanaryMessage process(QanaryMessage myQanaryMessage) throws Exception {
		logger.info("process: {}", myQanaryMessage);
		// TODO: implement processing of question
		QanaryUtils myQanaryUtils = this.getUtils(myQanaryMessage);
	    QanaryQuestion<String> myQanaryQuestion = new QanaryQuestion(myQanaryMessage);
	    String myQuestion = myQanaryQuestion.getTextualRepresentation();
	    ArrayList<Link> links = new ArrayList<Link>();
	    try {
			File f = new File("qanary_component-NED-Ontotext/src/main/resources/questions.txt");
		    FileReader fr = new FileReader(f);
		    BufferedReader br  = new BufferedReader(fr);
			int flag = 0;
			String line;
//			Object obj = parser.parse(new FileReader("DandelionNER.json"));
//			JSONObject jsonObject = (JSONObject) obj;
//			Iterator<?> keys = jsonObject.keys();
				
			while((line = br.readLine()) != null && flag == 0) {
				String question = line.substring(0, line.indexOf("Answer:"));
				logger.info("{}", line);
				logger.info("{}", myQuestion);
				if(question.trim().equals(myQuestion)){
				    String Answer = line.substring(line.indexOf("Answer:")+"Answer:".length());
				    logger.info("Here {}", Answer);
				    Answer = Answer.trim();
				    JSONArray jsonArr =new JSONArray(Answer);
				    if(jsonArr.length()!=0){
				    	for (int i = 0; i < jsonArr.length(); i++) {
				    		JSONObject explrObject = jsonArr.getJSONObject(i);  
				    		logger.info("Question: {}", explrObject);
				    		Link l = new Link();
			    	        l.begin = (int) explrObject.get("begin");
			    	        l.end = (int) explrObject.get("end")+1;
			    	        l.link= explrObject.getString("link");
			                links.add(l);
			            }
			           }
				    flag=1;
				    break;	
				}
			}
			br.close();
			if(flag==0)	{
				logger.info("Question: {}", myQuestion);
				//STEP2
				HttpClient httpclient = HttpClients.createDefault();
				HttpPost httppost = new HttpPost("http://tag.ontotext.com/ces-en/extract");
				httppost.addHeader("X-JwtToken", "<JWT Token goes here>");
				httppost.addHeader("Accept", "application/vnd.ontotext.ces+json");
				httppost.addHeader("Content-Type","text/plain");
				httppost.setEntity(new StringEntity(myQuestion));
				try {
					HttpResponse response = httpclient.execute(httppost);
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						InputStream instream = entity.getContent();
						String text = IOUtils.toString(instream, StandardCharsets.UTF_8.name());
						JSONObject jsonObject = new JSONObject(text);
						JSONArray jsonArray = jsonObject.getJSONArray("mentions"); 
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject explrObject = jsonArray.getJSONObject(i);
							int begin = (int) explrObject.get("startOffset");
							int end = (int) explrObject.get("endOffset");
							if(explrObject.has("features")) {
								JSONObject features =(JSONObject) explrObject.get("features");
								if(features.has("exactMatch")) {
									JSONArray uri = features.getJSONArray("exactMatch"); 
									String uriLink =  uri.getString(0);
									logger.info("Question: {}", explrObject);
									logger.info("Question: {}", begin);
									logger.info("Question: {}", end);
									Link l = new Link();
									l.begin = begin;
									l.end = end;
									l.link = uriLink;
									links.add(l);
									}
							}
						}
//	            JSONObject jsnobject = new JSONObject(text);
//	            JSONArray jsonArray = jsnobject.getJSONArray("endOffset");
//	            for (int i = 0; i < jsonArray.length(); i++) {
//	                JSONObject explrObject = jsonArray.getJSONObject(i);
//	                logger.info("JSONObject: {}", explrObject);
//	                logger.info("JSONArray: {}", jsonArray.getJSONObject(i));
//	                //logger.info("Question: {}", text);
//	                
//	        }
						logger.info("Question: {}", text);
						logger.info("Question: {}", jsonArray);
						try {
							// do something useful
						} finally {
							instream.close();
						}
					}
	        BufferedWriter buffWriter = new BufferedWriter(new FileWriter("qanary_component-NED-Ontotext/src/main/resources/questions.txt", true));
	        Gson gson = new Gson();
	        
	        String json = gson.toJson(links);
	        logger.info("gsonwala: {}",json);
	        
	        String MainString = myQuestion + " Answer: "+json;
	        buffWriter.append(MainString);
	        buffWriter.newLine();
	        buffWriter.close();
	        }
				catch (ClientProtocolException e) {
					logger.info("Exception: {}", myQuestion);
					// TODO Auto-generated catch block
		 		} 
				catch (IOException e1) {
	    	logger.info("Except: {}", e1);
	        // TODO Auto-generated catch block
	    		}
			}
		}
	
	    catch(FileNotFoundException e) 
		{ 
		    //handle this
			logger.info("{}", e);
		}
		logger.info("store data in graph {}", myQanaryMessage.getValues().get(myQanaryMessage.getEndpoint()));
		// TODO: insert data in QanaryMessage.outgraph

		logger.info("apply vocabulary alignment on outgraph");
		// TODO: implement this (custom for every component)
		for (Link l : links) {
            String sparql = "PREFIX qa: <http://www.wdaqua.eu/qa#> " //
                    + "PREFIX oa: <http://www.w3.org/ns/openannotation/core/> " //
                    + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " //
                    + "INSERT { " + "GRAPH <" + myQanaryQuestion.getOutGraph() + "> { " //
                    + "  ?a a qa:AnnotationOfInstance . " //
                    + "  ?a oa:hasTarget [ " //
                    + "           a    oa:SpecificResource; " //
                    + "           oa:hasSource    <" + myQanaryQuestion.getUri() + ">; " //
                    + "           oa:hasSelector  [ " //
                    + "                    a oa:TextPositionSelector ; " //
                    + "                    oa:start \"" + l.begin + "\"^^xsd:nonNegativeInteger ; " //
                    + "                    oa:end  \"" + l.end + "\"^^xsd:nonNegativeInteger  " //
                    + "           ] " //
                    + "  ] . " //
                    + "  ?a oa:hasBody <" + l.link + "> ;" //
                    + "     oa:annotatedBy <https://ontotextNED.com> ; " //
                    + "	    oa:AnnotatedAt ?time  " + "}} " //
                    + "WHERE { " //
                    + "  BIND (IRI(str(RAND())) AS ?a) ."//
                    + "  BIND (now() as ?time) " //
                    + "}";
            logger.debug("Sparql query: {}", sparql);
            myQanaryUtils.updateTripleStore(sparql);
        }
		return myQanaryMessage;
	}
	class Link {
        public int begin;
        public int end;
        public String link;
    }
}
