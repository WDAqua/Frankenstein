package eu.wdaqua.qanary.clsnliod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.wdaqua.qanary.commons.QanaryMessage;
import eu.wdaqua.qanary.commons.QanaryQuestion;
import eu.wdaqua.qanary.commons.QanaryUtils;
import eu.wdaqua.qanary.component.QanaryComponent;
import eu.wdaqua.qanary.clsnliod.DbpediaRecorodClass;


@Component
/**
 * This component connected automatically to the Qanary pipeline.
 * The Qanary pipeline endpoint defined in application.properties (spring.boot.admin.url)
 * @see <a href="https://github.com/WDAqua/Qanary/wiki/How-do-I-integrate-a-new-component-in-Qanary%3F" target="_top">Github wiki howto</a>
 */
public class ClsNliodCls extends QanaryComponent {
	private static final Logger logger = LoggerFactory.getLogger(ClsNliodCls.class);
	//private HashSet<String> dbLinkListSet = new HashSet<String>();

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
		ArrayList<Selection> selections = new ArrayList<Selection>();
		//String question = URLEncoder.encode(myQuestion, "UTF-8");
		//String question = "Which comic characters are painted by Bill Finger?";
        String language1 = "en";
        logger.info("Langauge of the Question: {}",language1);
        HashSet<String> dbLinkListSet = new HashSet<String>();
       
        try {
			File f = new File("questions.txt");
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
			    	Answer= Answer.substring(1,Answer.length()-1);
			    	String[] values = Answer.split(",");
			    	for(int i=0;i<values.length;i++)
			    	{
			    		dbLinkListSet.add(values[i]);
			    	}
			    	flag=1;
			    	break;	
			    }
			   
			    
			}
			br.close();
			if(flag==0)
			{
	
        
        
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
		output1= ClsNliodCls.runCurlPOSTWithParam(url, data, contentType);
		}catch(Exception e){}
//		System.out.println("The output template is:" +output1);
		logger.info("The output template is: {}",output1);
		PropertyRetrival propertyRetrival =  new PropertyRetrival();
        Property property= propertyRetrival.retrival(output1);
        
		List<MySelection> posLstl= new ArrayList<MySelection>();

				// for class
				for(String wrd:property.classRdf){
					MySelection ms = new MySelection();
					ms.type= "AnnotationOfClass";
					ms.rsc="SpecificClass";
					ms.word= wrd;
					ms.begin=myQuestion.indexOf(wrd);
					ms.end=ms.begin+wrd.length();
					posLstl.add(ms);
		            System.out.println("classRdf: "+wrd);
				
				logger.info("Apply vocabulary alignment on outgraph");
	          
				String dbpediaClass = null;
				try {
				String myKey1 = wrd.trim();
				if(myKey1!=null && !myKey1.equals("")) {
				System.out.println("searchDbLinkInTTL: "+myKey1);
					for (Entry<String, String> e : DbpediaRecorodClass.get().tailMap(myKey1).entrySet()) {
						    if(e.getKey().contains(myKey1)) 
						    {
						    	dbpediaClass = e.getValue();
						      	break;
						    }
							ArrayList<String> strArrayList = new ArrayList<String>(Arrays.asList(e.getKey().split("\\s+")));
							//System.out.println(strArrayList.toString());
							for (String s : strArrayList)
							{
							    if(myKey1.compareTo(s) == 0) {
							    	dbpediaClass = e.getValue();
							 }
							}
							 
							 if(dbpediaClass!=null)
							 break;
							    
							 }
		         
				}
				} catch (Exception e) {
					// logger.info("Except: {}", e);
					// TODO Auto-generated catch block
				}
				if(dbpediaClass!=null)
				dbLinkListSet.add(dbpediaClass);
				System.out.println("searchDbLinkInTTL: "+dbpediaClass);
			}
BufferedWriter buffWriter = new BufferedWriter(new FileWriter("questions.txt", true));
		       
		        
		        String MainString = myQuestion + " Answer: "+dbLinkListSet.toString();
		        buffWriter.append(MainString);
		        buffWriter.newLine();
		        buffWriter.close();
			}
		}
		 catch(FileNotFoundException e) 
			{ 
			    //handle this
				logger.info("{}", e);
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
	                 + "     oa:annotatedBy <http://ClsNliodCls.com> ; "
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
	class Selection {
		public int begin;
		public int end;
	}
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
}
