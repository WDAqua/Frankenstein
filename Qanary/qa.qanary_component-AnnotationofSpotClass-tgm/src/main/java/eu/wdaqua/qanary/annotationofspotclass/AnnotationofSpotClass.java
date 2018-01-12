package eu.wdaqua.qanary.annotationofspotclass;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
public class AnnotationofSpotClass extends QanaryComponent {
	private static final Logger logger = LoggerFactory.getLogger(AnnotationofSpotClass.class);

	
	/**
     * runCurlPOSTWithParam is a function to fetch the response from a CURL command using POST.
     */
    public static String runCurlPOSTWithParam(String weburl,String data,String contentType) throws Exception
	{
		
    	
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

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			xmlResp = response.toString();

			System.out.println("Curl Response: \n" + xmlResp);
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
		logger.info("process: {}", myQanaryMessage);
		QanaryUtils myQanaryUtils = this.getUtils(myQanaryMessage);
		QanaryQuestion<String> myQanaryQuestion = new QanaryQuestion(myQanaryMessage);
		String myQuestion = myQanaryQuestion.getTextualRepresentation();
		String question = "";
		question = URLEncoder.encode(myQuestion, "UTF-8");
        String questionlang = "";
        String language1 = "en";
        logger.info("Langauge of the Question: {}",language1);
        
       
        
        String url = "";
		String data = "";
		String contentType = "application/json";
		 
		//url = "http://ws.okbqa.org:1515/templategeneration/rocknrole";
		url  = "http://121.254.173.90:1515/templategeneration/rocknrole";
		data = "{  \"string\":\""+myQuestion+"\",\"language\":\""+language1+"\"}";//"{  \"string\": \"Which river flows through Seoul?\",  \"language\": \"en\"}";
		System.out.println("\ndata :" +data);
		System.out.println("\nComponent : 21");
		String output1="";
		// pass the input in CURL command and call the function.
		
		try
		{
		output1= AnnotationofSpotClass.runCurlPOSTWithParam(url, data, contentType);
		}catch(Exception e){}
//		System.out.println("The output template is:" +output1);
		logger.info("The output template is: {}",output1);

        Property property= PropertyRetrival.retrival(output1);
        
		List<MySelection> posLstl= new ArrayList<MySelection>();

				// for class
				for(String wrd:property.classRdf){
					MySelection ms = new MySelection();
					ms.type= "AnnotationOfSpotClass";
					ms.rsc="SpecificClass";
					ms.word= wrd;
					ms.begin=question.indexOf(wrd);
					ms.end=ms.begin+wrd.length();
					posLstl.add(ms);
		            System.out.println("classRdf: "+wrd);
				}
				
				logger.info("Apply vocabulary alignment on outgraph");
	          
	           
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
