package eu.wdaqua.qanary.aylien;

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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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
public class AylienNED extends QanaryComponent {
	private static final Logger logger = LoggerFactory.getLogger(AylienNED.class);

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
			File f = new File("qanary_component-NED-Aylien/src/main/resources/questions.txt");
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
	    
	    
	    logger.info("Question {}", myQuestion);
	    String thePath = URLEncoder.encode(myQuestion, "UTF-8"); 
	    logger.info("Path {}", thePath);
	      
	    HttpClient httpclient = HttpClients.createDefault();
	    HttpGet httpget = new HttpGet("https://api.aylien.com/api/v1/concepts?text="+thePath);
	    //httpget.addHeader("User-Agent", USER_AGENT);
	    httpget.addHeader("X-AYLIEN-TextAPI-Application-Key", "c7f250facfa39df49bb614af1c7b04f7");
	    httpget.addHeader("X-AYLIEN-TextAPI-Application-ID", "6b3e5a8d");
	    HttpResponse response = httpclient.execute(httpget);
	    try {    	 
	    	HttpEntity entity = response.getEntity();
	        if (entity != null) {
	        	InputStream instream = entity.getContent();
	        	// String result = getStringFromInputStream(instream);
	        	String text = IOUtils.toString(instream, StandardCharsets.UTF_8.name());
	        	JSONObject response2 = new JSONObject(text); 
	        	//logger.info("JA: {}", response2);
	        	JSONObject concepts = (JSONObject) response2.get("concepts");
	        	logger.info("JA: {}", concepts);
	        	ArrayList<String> list = new ArrayList<String>(concepts.keySet());
	        	logger.info("JA: {}", list);
	        	for(int i=0;i<list.size();i++){
	        		JSONObject explrObj = (JSONObject) concepts.get(list.get(i));
	        		if(explrObj.has("surfaceForms")){
	        			JSONArray jsonArray =(JSONArray) explrObj.get("surfaceForms");
	        			JSONObject explrObj2 = (JSONObject) jsonArray.get(0);
	        			int begin = (int) explrObj2.get("offset");
	        			String endString = (String) explrObj2.get("string");
	        			int end = begin +endString.length();
	        			//logger.info("Question: {}", explrObj2);
	        			logger.info("Start: {}", begin);
	        			logger.info("End: {}", end);
	        			String finalUri = list.get(i);
	        		
	        			Link l = new Link();
	        			l.begin = begin;
	        			l.end = end;
	        			l.link= finalUri;
	        			links.add(l);
	        		}
	        	}
	        }
	        BufferedWriter buffWriter = new BufferedWriter(new FileWriter("qanary_component-NED-Aylien/src/main/resources/questions.txt", true));
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
                    + "     oa:annotatedBy <https://api.aylien.com/api/v1/concepts> ; " //
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
