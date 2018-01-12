package eu.wdaqua.qanary.relationlinker2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
public class RelationLinker2 extends QanaryComponent {
	private static final Logger logger = LoggerFactory.getLogger(RelationLinker2.class);

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
	        try {
				File f = new File("qanary_component-REL-RelationLinker2/src/main/resources/questions.txt");
		    	FileReader fr = new FileReader(f);
		    	BufferedReader br  = new BufferedReader(fr);
				int flag = 0;
				String line;
//				Object obj = parser.parse(new FileReader("DandelionNER.json"));
//				JSONObject jsonObject = (JSONObject) obj;
//				Iterator<?> keys = jsonObject.keys();
				
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
			    	                l.end = (int) explrObject.get("end");
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
	        
	        //STEP2
	        HttpClient httpclient = HttpClients.createDefault();
	        HttpPost httppost = new HttpPost("http://localhost:8097");
	         httppost.addHeader("Accept", "application/json");
	      
	        httppost.setEntity(new StringEntity(myQuestion));
	        try {
	        HttpResponse response = httpclient.execute(httppost);
	        HttpEntity entity = response.getEntity();
	        if (entity != null) {
	        	InputStream instream = entity.getContent();
	           // String result = getStringFromInputStream(instream);
	            String text2 = IOUtils.toString(instream, StandardCharsets.UTF_8.name());
	           
	            String text = text2.substring(text2.indexOf('{'));
	            logger.info("Question: {}", text);
	            JSONObject jsonObject = new JSONObject(text);
	            ArrayList<String> list = new ArrayList<String>(jsonObject.keySet());
	            JSONObject jsonObj = (JSONObject) jsonObject.get(list.get(0));
	            logger.info("test {}", list);
	            logger.info("test {}", jsonObj);
	            JSONArray jsonArray = (JSONArray) jsonObj.get("0");
	            logger.info("test {}", jsonArray);
	            String test = (String) jsonArray.get(1);
	            Link l = new Link();
	                l.begin = myQuestion.indexOf(list.get(1));
	                l.end = l.begin + list.get(1).length();
	                l.link = test;
	                links.add(l);
//	            for (int i = 0; i < jsonArray.length(); i++) {
//	                JSONObject explrObject = jsonArray.getJSONObject(i);
//	                int begin = (int) explrObject.get("startOffset");
//	                int end = (int) explrObject.get("endOffset");
//	                if(explrObject.has("features"))
//	                {
//	                	JSONObject features =(JSONObject) explrObject.get("features");
//	                	if(features.has("exactMatch"))
//	                	{
//	                		JSONArray uri = features.getJSONArray("exactMatch"); 
//	                		String uriLink =  uri.getString(0);
//	                		logger.info("Question: {}", explrObject);
//	    	                logger.info("Question: {}", begin);
//	    	                logger.info("Question: {}", end);
//	                		Link l = new Link();
//	     	                l.begin = begin;
//	     	                l.end = end;
//	     	                l.link = uriLink;
//	     	                links.add(l);
//	                	}
//	                }
//	                
//	               
	           // }
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
	            //logger.info("Question: {}", jsonArray);
	            try {
	                // do something useful
	            } finally {
	                instream.close();
	            }
	        }
	        
	        BufferedWriter buffWriter = new BufferedWriter(new FileWriter("qanary_component-REL-RelationLinker2/src/main/resources/questions.txt", true));
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
	    } catch (IOException e1) {
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
		 String sparql = "prefix qa: <http://www.wdaqua.eu/qa#> "
               + "prefix oa: <http://www.w3.org/ns/openannotation/core/> "
               + "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
               + "prefix dbp: <http://dbpedia.org/property/> "
               + "INSERT { "
               + "GRAPH <" +  myQanaryQuestion.getOutGraph()  + "> { "
               + "  ?a a qa:AnnotationOfRelation . "
               + "  ?a oa:hasTarget [ "
               + "           a    oa:SpecificResource; "
               + "           oa:hasSource    <" + myQanaryQuestion.getUri() + ">; "
               + "              oa:start \"" + l.begin + "\"^^xsd:nonNegativeInteger ; " //
               + "              oa:end  \"" + l.end + "\"^^xsd:nonNegativeInteger  " //
               + "  ] ; "
               + "     oa:hasBody <" + l.link + "> ;" 
               + "     oa:annotatedBy <http://relationlinker2.com> ; "
               + "	    oa:AnnotatedAt ?time  "
               + "}} "
               + "WHERE { "
               + "BIND (IRI(str(RAND())) AS ?a) ."
               + "BIND (now() as ?time) "
               + "}";
       logger.info("Sparql query {}", sparql);
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
