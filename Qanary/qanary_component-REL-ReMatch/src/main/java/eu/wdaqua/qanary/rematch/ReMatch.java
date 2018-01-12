package eu.wdaqua.qanary.rematch;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
public class ReMatch extends QanaryComponent {
	private static final Logger logger = LoggerFactory.getLogger(ReMatch.class);

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

	        logger.info("Question: {}", myQuestion);
	        //STEP2
	        HttpClient httpclient = HttpClients.createDefault();
	        myQuestion = java.net.URLEncoder.encode(myQuestion, "UTF-8").replaceAll("\\+", "%20");
	        HttpGet httpget = new HttpGet("http://94.130.104.232:8081/"+myQuestion);
	        try {
	        HttpResponse response = httpclient.execute(httpget);
	        HttpEntity entity = response.getEntity();
	        if (entity != null) {
	        	InputStream instream = entity.getContent();
	           // String result = getStringFromInputStream(instream);
	            String text = IOUtils.toString(instream, StandardCharsets.UTF_8.name());
	           
	            logger.info("Question: {}", text);
	            JSONObject jsonObject = new JSONObject(text);
	            //ArrayList<String> list = new ArrayList<String>(jsonObject.keySet());
	            int flag=0;
	            Iterator<?> keys = jsonObject.keys();
	            while( keys.hasNext() ) {
	            	String key = (String)keys.next();
	            	logger.info(key);
	            }
	            
//	            for(int i = 1;flag!=1;i++)
//	            {
//	            	if(jsonObject.has("relation "+i))
//	            	{
//	            		String str = (String) jsonObject.get("relation "+i);
//	            		logger.info("test {}", str);
//	            		String test = "http://dbpedia.org/ontology/"+str;
//	            		Link l = new Link();
//	            		l.begin = myQuestion.indexOf(str);
//	            		l.end = l.begin + str.length();
//	            		l.link = test;
//	            		links.add(l);
//	            	}
//	            	else
//	            		flag=1;
//	            }
	        }
	        }
		 catch (ClientProtocolException e) {
			 logger.info("Exception: {}", myQuestion);
	        // TODO Auto-generated catch block
	    } catch (IOException e1) {
	    	logger.info("Except: {}", e1);
	        // TODO Auto-generated catch block
	    }

		logger.info("store data in graph {}", myQanaryMessage.getValues().get(myQanaryMessage.getEndpoint()));
		// TODO: insert data in QanaryMessage.outgraph

		logger.info("apply vocabulary alignment on outgraph");
		// TODO: implement this (custom for every component)
//		for (Link l : links) {
//			 String sparql = "prefix qa: <http://www.wdaqua.eu/qa#> "
//	                 + "prefix oa: <http://www.w3.org/ns/openannotation/core/> "
//	                 + "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
//	                 + "prefix dbp: <http://dbpedia.org/property/> "
//	                 + "INSERT { "
//	                 + "GRAPH <" +  myQanaryQuestion.getOutGraph()  + "> { "
//	                 + "  ?a a qa:AnnotationOfRelation . "
//	                 + "  ?a oa:hasTarget [ "
//	                 + "           a    oa:SpecificResource; "
//	                 + "           oa:hasSource    <" + myQanaryQuestion.getUri() + ">; "
//	                 + "              oa:start \"" + l.begin + "\"^^xsd:nonNegativeInteger ; " //
//	                 + "              oa:end  \"" + l.end + "\"^^xsd:nonNegativeInteger  " //
//	                 + "  ] ; "
//	                 + "     oa:hasBody <" + l.link + "> ;" 
//	                 + "     oa:annotatedBy <http://rematch.com> ; "
//	                 + "	    oa:AnnotatedAt ?time  "
//	                 + "}} "
//	                 + "WHERE { "
//	                 + "BIND (IRI(str(RAND())) AS ?a) ."
//	                 + "BIND (now() as ?time) "
//	                 + "}";
//	         logger.info("Sparql query {}", sparql);
//	         myQanaryUtils.updateTripleStore(sparql);
//			}
	 
		return myQanaryMessage;
	}
	class Link {
        public int begin;
        public int end;
        public String link;
    }
}
