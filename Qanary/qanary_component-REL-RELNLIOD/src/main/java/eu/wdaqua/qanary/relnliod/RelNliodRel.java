package eu.wdaqua.qanary.relnliod;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;



import eu.wdaqua.qanary.commons.QanaryMessage;
import eu.wdaqua.qanary.commons.QanaryQuestion;
import eu.wdaqua.qanary.commons.QanaryUtils;
import eu.wdaqua.qanary.component.QanaryComponent;
import eu.wdaqua.qanary.relnliod.DbpediaRecorodProperty;
import javassist.bytecode.Descriptor.Iterator;

@Component
/**
 * This component connected automatically to the Qanary pipeline. The Qanary
 * pipeline endpoint defined in application.properties (spring.boot.admin.url)
 * 
 * @see <a href=
 *      "https://github.com/WDAqua/Qanary/wiki/How-do-I-integrate-a-new-component-in-Qanary%3F"
 *      target="_top">Github wiki howto</a>
 */
public class RelNliodRel extends QanaryComponent {
	private static final Logger logger = LoggerFactory.getLogger(RelNliodRel.class);
	//private HashSet<String> dbLinkListSet = new HashSet<String>();
	//private boolean  relationsFlag = false;

	/**
	 * implement this method encapsulating the functionality of your Qanary
	 * component
	 * 
	 * @throws Exception
	 */
	@Override
	public QanaryMessage process(QanaryMessage myQanaryMessage) throws Exception {
		boolean  relationsFlag = false;
		HashSet<String> dbLinkListSet = new HashSet<String>();
		logger.info("process: {}", myQanaryMessage);
		QanaryUtils myQanaryUtils = this.getUtils(myQanaryMessage);
		QanaryQuestion<String> myQanaryQuestion = new QanaryQuestion(myQanaryMessage);
		String myQuestion = myQanaryQuestion.getTextualRepresentation();
		//String question = URLEncoder.encode(myQuestion, "UTF-8");
		ArrayList<Selection> selections = new ArrayList<Selection>();

		 try {
				File f = new File("questions.txt");
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
		
		
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost("http://api.textrazor.com");
		httppost.setHeader("x-textrazor-key", "4cc373915018a40c921da8243995b9a316a8d2716048acc3b900fb2a");
		httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("text", myQuestion.toLowerCase()));
		params.add(new BasicNameValuePair("extractors", "relations,words"));
		httppost.setEntity(new UrlEncodedFormEntity(params));
		TextRazorDbSearch textRazorDbSearch = new TextRazorDbSearch();
		try {
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				String text = IOUtils.toString(instream, StandardCharsets.UTF_8.name());
				System.out.println(text);
				JSONObject response1 = (new JSONObject(text)).getJSONObject("response");
				JSONArray jsonArraySent = (JSONArray) response1.get("sentences");
				ArrayList<String> arrayListWords = textRazorDbSearch.createArrayWordsList(jsonArraySent);
				System.out.println(arrayListWords.toString());
				textRazorDbSearch.createPropertyList(response1);
				textRazorDbSearch.createDbLinkListSet(arrayListWords);
				dbLinkListSet  = textRazorDbSearch.getDbLinkListSet();
				relationsFlag = textRazorDbSearch.getRelationsFlag();
				if(dbLinkListSet.isEmpty() && relationsFlag ==true) {
					textRazorDbSearch.createRePropertyList(response1);
					textRazorDbSearch.createDbLinkListSet(arrayListWords);
				}
				dbLinkListSet  = textRazorDbSearch.getDbLinkListSet();
				System.out.print("DbLinkListSet: "+dbLinkListSet.toString());

				
				BufferedWriter buffWriter = new BufferedWriter(new FileWriter("questions.txt", true));
		       
		        
		        String MainString = myQuestion + " Answer: "+dbLinkListSet.toString();
		        buffWriter.append(MainString);
		        buffWriter.newLine();
		        buffWriter.close();
			}
		} catch (ClientProtocolException e) {
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
		                 + "  ?a a qa:AnnotationOfRelation . "
		                 + "  ?a oa:hasTarget [ "
		                 + "           a    oa:SpecificResource; "
		                 + "           oa:hasSource    <" + myQanaryQuestion.getUri() + ">; "
		                 + "  ] ; "
		                 + "     oa:hasBody <" + urls + "> ;" 
		                 + "     oa:annotatedBy <http://RelNliodRel.com> ; "
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
}