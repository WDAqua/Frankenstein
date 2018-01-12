package eu.wdaqua.qanary.annotationofspotproperty;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
public class AnnotationofSpotProperty extends QanaryComponent {
	private static final Logger logger = LoggerFactory.getLogger(AnnotationofSpotProperty.class);
	/**
     * runCurlPOSTWithParam is a function to fetch the response from a CURL command using POST.
     */
    public static String runCurlPOSTWithParam(String weburl,String data,String contentType) throws Exception
	{
		
    	
    	//The String xmlResp is to store the output of the Template generator web service accessed via CURL command
    	
        String xmlResp = "";
        try {
        	URL url = new URL(weburl);
    		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    		
    		connection.setRequestMethod("POST");
    		connection.setDoOutput(true);
    		
    		connection.setRequestProperty("Content-Type", contentType);
    				
    		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
    		wr.writeBytes(data);
    		wr.flush();
    		wr.close();
    		
    		
    	
    		BufferedReader in = new BufferedReader(
    		        new InputStreamReader(connection.getInputStream()));
    		String inputLine;
    		StringBuffer response = new StringBuffer();

    		while ((inputLine = in.readLine()) != null) {
    			response.append(inputLine);
    		}
    		in.close();
    		xmlResp = response.toString();
    		
    		System.out.println("Curl Response: \n"+xmlResp);
            logger.info("Response {}", xmlResp);
        } catch (Exception e) {
        }
        return (xmlResp);

	}
	
	
	/**
	 * implement this method encapsulating the functionality of your Qanary
	 * component
	 * @throws Exception 
	 */
	@Override
	public QanaryMessage process(QanaryMessage myQanaryMessage) throws Exception {
		HashSet<String> dbLinkListSet = new HashSet<String>();
		logger.info("process: {}", myQanaryMessage);
		QanaryUtils myQanaryUtils = this.getUtils(myQanaryMessage);
		QanaryQuestion<String> myQanaryQuestion = new QanaryQuestion(myQanaryMessage);
		String myQuestion = myQanaryQuestion.getTextualRepresentation();
		ArrayList<Selection> selections = new ArrayList<Selection>();
	    //String question = URLEncoder.encode(myQuestion, "UTF-8");
		//String question = "Which comic characters are painted by Bill Finger?";
        String language1 = "en";
        logger.info("Langauge of the Question: {}",language1);
        
       
       // String question =  "In which monarch did Li Si succeed someone";
        String url = "";
		String data = "";
		String contentType = "application/json";
		 
		url = "http://ws.okbqa.org:1515/templategeneration/rocknrole";
		//url  = "http://121.254.173.90:1515/templategeneration/rocknrole";
		data = "{  \"string\":\""+myQuestion+"\",\"language\":\""+language1+"\"}";
		System.out.println("\ndata :" +data);
		System.out.println("\nComponent : 21");
		String output1="";
		// pass the input in CURL command and call the function.
		
		try
		{
		output1= AnnotationofSpotProperty.runCurlPOSTWithParam(url, data, contentType);
		}catch(Exception e){}
//		System.out.println("The output template is:" +output1);
		logger.info("The output template is: {}",output1);

		PropertyRetrival propertyRetrival =  new PropertyRetrival();
        Property property= propertyRetrival.retrival(output1);
        
		List<MySelection> posLstl= new ArrayList<MySelection>();

				// for class
				for(String wrd:property.property){
					MySelection ms = new MySelection();
					ms.type= "AnnotationOfClass";
					ms.rsc="SpecificClass";
					ms.word= wrd;
					ms.begin=myQuestion.indexOf(wrd);
					ms.end=ms.begin+wrd.length();
					posLstl.add(ms);
		            System.out.println("Property: "+wrd);
				
				logger.info("Apply vocabulary alignment on outgraph");
	          
				String dbpediaProperty = null;
				try {
				String myKey1 = wrd.trim();
				if(myKey1!=null && !myKey1.equals("")) {
				System.out.println("searchDbLinkInTTL: "+myKey1);
					for (Entry<String, String> e : DbpediaRecorodProperty.get().tailMap(myKey1).entrySet()) {
						    if(e.getKey().contains(myKey1)) 
						    {
						    	dbpediaProperty = e.getValue();
						      	break;
						    }
							ArrayList<String> strArrayList = new ArrayList<String>(Arrays.asList(e.getKey().split("\\s+")));
							//System.out.println(strArrayList.toString());
							for (String s : strArrayList)
							{
							    if(myKey1.compareTo(s) == 0) {
							    	dbpediaProperty = e.getValue();
							 }
							}
							 
							 if(dbpediaProperty!=null)
							 break;
							    
							 }
		         
				}
				} catch (Exception e) {
					// logger.info("Except: {}", e);
					// TODO Auto-generated catch block
				}
				if(dbpediaProperty!=null)
				dbLinkListSet.add(dbpediaProperty);
				System.out.println("searchDbLinkInTTL: "+ dbpediaProperty);
			}
		System.out.println("DbLinkListSet : " +dbLinkListSet.toString());
		logger.info("store data in graph {}", myQanaryMessage.getValues().get(myQanaryMessage.getEndpoint()));
		// TODO: insert data in QanaryMessage.outgraph

		logger.info("apply vocabulary alignment on outgraph");
		// TODO: implement this (custom for every component)
		for (String urls : dbLinkListSet) {
			 String sparql = "prefix qa: <http://www.wdaqua.eu/qa#> "
	                 + "prefix oa: <http://www.w3.org/ns/openannotation/core/> "
	                 + "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
	                 + "prefix dbp: <http://dbpedia.org/property/> "
	                 + "INSERT { "
	                 + "GRAPH <" +  myQanaryQuestion.getOutGraph()  + "> { "
	                 + "  ?a a qa:AnnotationOfClass . "
	                 + "  ?a oa:hasTarget [ "
	                 + "           a    oa:SpecificResource; "
	                 + "           oa:hasSource    <" + myQanaryQuestion.getUri() + ">; "
	                 + "  ] ; "
	                 + "     oa:hasBody <" + urls + "> ;" 
	                 + "     oa:annotatedBy <http://AnnotationofSpotProperty.com> ; "
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

	private void loadTripleStore(String sparqlQuery, String endpoint) {
        UpdateRequest request = UpdateFactory.create(sparqlQuery);
        UpdateProcessor proc = UpdateExecutionFactory.createRemote(request, endpoint);
        proc.execute();
    }

    private ResultSet selectTripleStore(String sparqlQuery, String endpoint) {
        Query query = QueryFactory.create(sparqlQuery);
        QueryExecution qExe = QueryExecutionFactory.sparqlService(endpoint, query);
        return qExe.execSelect();
    }
    private class Selection {
        public int begin;
        public int end;
    }

}
