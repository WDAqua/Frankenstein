package eu.wdaqua.qanary.spotlightNED;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import eu.wdaqua.qanary.commons.QanaryMessage;
import eu.wdaqua.qanary.commons.QanaryQuestion;
import eu.wdaqua.qanary.commons.QanaryUtils;
import eu.wdaqua.qanary.component.QanaryComponent;

/**
 * represents a wrapper of the DBpedia Spotlight as NED
 *
 * @author Kuldeep Singh, Dennis Diefenbach, Arun Sethupat
 */

@Component
public class DBpediaSpotlightNED extends QanaryComponent {
    private static final Logger logger = LoggerFactory.getLogger(DBpediaSpotlightNED.class);
    private String service = "http://spotlight.sztaki.hu:2222/rest/disambiguate/";

    public QanaryMessage process(QanaryMessage myQanaryMessage) throws Exception {
        logger.info("process: {}", myQanaryMessage);


        long startTime = System.currentTimeMillis();
        //STEP 1: Retrive the information needed for the computations

        // retrive the question
        QanaryUtils myQanaryUtils = this.getUtils(myQanaryMessage);
        QanaryQuestion<String> myQanaryQuestion = this.getQanaryQuestion(myQanaryMessage);
        String myQuestion = myQanaryQuestion.getTextualRepresentation();
        //String myQuestion = "Is Michelle Obama a wife of Barack Obama ?";
        logger.info("Question: {}", myQuestion);
        ArrayList<Link> links = new ArrayList<Link>();
        try {
			File f = new File("qanary_component-NED-DBpedia-Spotlight/src/main/resources/questions.txt");
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
				
			    if(question.trim().equals(myQuestion))
			    {
			    	String Answer = line.substring(line.indexOf("Answer:")+"Answer:".length());
			    	logger.info("Here {}", Answer);
			    	Answer = Answer.trim();
			    	JSONArray jsonArr =new JSONArray(Answer);
			    	if(jsonArr.length()!=0)
	 	        	   {
	 	        		   for (int i = 0; i < jsonArr.length(); i++) 
	 	        		   {
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
			if(flag==0)
			{
        
        // Retrieves the spots from the knowledge graph
        String madeUrlFromInput = "http://model.dbpedia-spotlight.org/en/annotate?text=";
		/*
		 * String qns[] = input.split(" "); String append = String.join("%20",
		 * qns);
		 */
        try {
            logger.info("Input is: {}", myQuestion);
            madeUrlFromInput += URLEncoder.encode(myQuestion, "UTF-8");
            HttpClient httpclient = HttpClients.createDefault();
    	    HttpGet httpget = new HttpGet(madeUrlFromInput);
    	    //httpget.addHeader("User-Agent", USER_AGENT);
    	    httpget.addHeader("Accept", "application/json");
    	    HttpResponse response = httpclient.execute(httpget);
  	      	try {
  	      		HttpEntity entity = response.getEntity();
  	      		if (entity != null) {
  	      			InputStream instream = entity.getContent();
  	      			String text = IOUtils.toString(instream, StandardCharsets.UTF_8.name());
  	      			JSONObject response2 = new JSONObject(text); 
  	      			logger.info("response2: {}", response2);
  	      			
  	      			if(response2.has("Resources")) {
  	      				JSONArray jsonArray =(JSONArray) response2.get("Resources");
  	      				for(int i=0;i<jsonArray.length();i++) {
  	      					JSONObject explrObj2 = (JSONObject) jsonArray.get(i);
  	      					int begin = Integer.parseInt((String) explrObj2.get("@offset"));
  	      					String endString = (String) explrObj2.get("@surfaceForm");
  	      					String uri = (String)explrObj2.get("@URI");
  	      					int end = begin +endString.length();
  	      					
  	      					logger.info("Begin: {}", begin);
  	      					logger.info("End: {}", end);
  	      				    logger.info("uri: {}", uri);
  	      					Link l = new Link();
  	      					l.begin = begin;
  	      					l.end = end;
  	      					l.link = uri;
  	      					links.add(l);
  	      				}
  	      			}
  	      		}
  	      	BufferedWriter buffWriter = new BufferedWriter(new FileWriter("qanary_component-NED-DBpedia-Spotlight/src/main/resources/questions.txt", true));
	        Gson gson = new Gson();
	        
	        String json = gson.toJson(links);
	        logger.info("gsonwala: {}",json);
	        
	        String MainString = myQuestion + " Answer: "+json;
	        buffWriter.append(MainString);
	        buffWriter.newLine();
	        buffWriter.close();
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
    }
   	}
   	    catch(FileNotFoundException e) 
   		{ 
   		    //handle this
   			logger.info("{}", e);
   		}
        // STEP2: Call the DBpedia NED service

        // it will create XML content, which needs to be input in DBpedia
        // NED with curl command
        
        // STEP3: Push the result of the component to the triplestore
        // long startTime = System.currentTimeMillis();

        // TODO: prevent that duplicate entries are created within the
        // triplestore, here the same data is added as already exit (see
        // previous SELECT query)
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
                    + "     oa:annotatedBy <https://github.com/dbpedia-spotlight/dbpedia-spotlight> ; " //
                    + "	    oa:AnnotatedAt ?time  " + "}} " //
                    + "WHERE { " //
                    + "  BIND (IRI(str(RAND())) AS ?a) ."//
                    + "  BIND (now() as ?time) " //
                    + "}";
            logger.debug("Sparql query: {}", sparql);
            myQanaryUtils.updateTripleStore(sparql);
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        logger.info("Time {}", estimatedTime);

        return myQanaryMessage;
    }

    class Link {
        public int begin;
        public int end;
        public String link;
    }

}
