package eu.wdaqua.qanary.diambiguationproperty;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import eu.wdaqua.qanary.commons.QanaryMessage;
import eu.wdaqua.qanary.component.QanaryComponent;

@Component
/**
 * This component connected automatically to the Qanary pipeline. The Qanary
 * pipeline endpoint defined in application.properties (spring.boot.admin.url)
 * 
 * @see <a href=
 *      "https://github.com/WDAqua/Qanary/wiki/How-do-I-integrate-a-new-component-in-Qanary%3F"
 *      target="_top">Github wiki howto</a>
 */
public class DiambiguationProperty extends QanaryComponent {
	private static final Logger logger = LoggerFactory.getLogger(DiambiguationProperty.class);

	
	public static String runCurlGetWithParam(String weburl, String data, String contentType)
			throws ClientProtocolException, IOException {
		String xmlResp = "";

		URL url = new URL(weburl+"data="+URLEncoder.encode(data,"UTF-8"));
    	//URL url = new URL(weburl+"?data="+URLEncoder.encode(data,"UTF-8"));
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setRequestMethod("GET");
		
		//connection.setDoOutput(true);
		//connection.setRequestProperty("data", data);
		connection.setRequestProperty("Content-Type", contentType);
				
		/*DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.writeBytes(data);
		wr.flush();
		wr.close();*/
		
		

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		xmlResp = response.toString();
		System.out.println("the curl Get output : "+ xmlResp);
		return xmlResp;
	}
	
	/**
	 * runCurlPOSTWithParam is a function to fetch the response from a CURL command
	 * using POST.
	 */
	public static String runCurlPOSTWithParam(String weburl, String data, String contentType) throws Exception {

		// The String xmlResp is to store the output of the Template generator web
		// service accessed via CURL command

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
	 */
	@Override
	public QanaryMessage process(QanaryMessage myQanaryMessage) {
		long startTime = System.currentTimeMillis();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		logger.info("process: {}", myQanaryMessage);
		// TODO: implement processing of question

		// STEP1: Retrieve the named graph and the endpoint
		String endpoint = myQanaryMessage.getEndpoint().toASCIIString();
		String namedGraph = myQanaryMessage.getInGraph().toASCIIString();
		System.out.println("Graph is" + namedGraph);
		logger.info("Endpoint: {}", endpoint);
		logger.info("InGraph: {}", namedGraph);
		// TODO: implement this (custom for every component)

		// STEP2: Retrieve information that are needed for the computations
		// Here, we need two parameters as input to be fetched from triplestore-
		// question and language of the question.
		// So first, Retrive the uri where the question is exposed
		String sparql = "PREFIX qa:<http://www.wdaqua.eu/qa#> " + "SELECT ?questionuri " + "FROM <" + namedGraph + "> "
				+ "WHERE {?questionuri a qa:Question}";

		ResultSet result = selectTripleStore(sparql, endpoint);
		String uriQuestion = result.next().getResource("questionuri").toString();
		logger.info("Uri of the question: {}", uriQuestion);
		// Retrive the question itself
		RestTemplate restTemplate = new RestTemplate();
		// TODO: pay attention to "/raw" maybe change that
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(uriQuestion + "/raw", String.class);
		String question = responseEntity.getBody();
		logger.info("Question: {}", question);

		// the below mentioned SPARQL query to fetch annotation of language from
		// triplestore
		String questionlang = "PREFIX qa:<http://www.wdaqua.eu/qa#> " + "SELECT ?lang " + "FROM <" + namedGraph + "> "
				+ "WHERE {?q a qa:Question ." + " ?anno <http://www.w3.org/ns/openannotation/core/hasTarget> ?q ."
				+ " ?anno <http://www.w3.org/ns/openannotation/core/hasBody> ?lang ."
				+ " ?anno a qa:AnnotationOfQuestionLanguage}";
		// Now fetch the language, in our case it is "en".
		ResultSet result1 = selectTripleStore(questionlang, endpoint);
		String language1 = "en";
		logger.info("Langauge of the Question: {}", language1);

		String url = "";
		String data = "";
		String contentType = "application/json";

		// http://repository.okbqa.org/components/21 is the template generator URL
		// Sample input for this is mentioned below.
		/*
		 * { "string": "Which river flows through Seoul?", "language": "en" }
		 * http://ws.okbqa.org:1515/templategeneration/rocknrole
		 */

		// now arrange the Web service and input parameters in the way, which is needed
		// for CURL command
		url = "http://121.254.173.90:1515/templategeneration/rocknrole";
		data = "{  \"string\":\"" + question + "\",\"language\":\"" + language1 + "\"}";// "{ \"string\": \"Which river
																						// flows through Seoul?\",
																						// \"language\": \"en\"}";
		System.out.println("\ndata :" + data);
		System.out.println("\nComponent : 21");
		String output1 = "";
		// pass the input in CURL command and call the function.

		try {
			output1 = DiambiguationProperty.runCurlPOSTWithParam(url, data, contentType);
		} catch (Exception e) {
		}
		System.out.println("The output template is:" + output1);

		/*
		 * once output is recieved, now the task is to parse the generated template, and
		 * store the needed information which is Resource, Property, Resource Literal,
		 * and Class. Then push this information back to triplestore The below code
		 * before step 4 does parse the template. For parsing PropertyRetrival.java file
		 * is used, which is in the same package.
		 * 
		 * for this, we create a new object property of Property class.
		 * 
		 */

		url = "http://121.254.173.90:2357/agdistis/run?";
		data = output1.substring(1,output1.length()-1);
		contentType = "application/json";

		System.out.println("\ndata :" + data);
		System.out.println("\nComponent : 7");
		output1 = "";
		try {
			output1 = DiambiguationProperty.runCurlGetWithParam(url, data, contentType);
		} catch (Exception e) {
		}
		System.out.println("The output template is:" + output1);

		JSONParser parser = new JSONParser();
		
		Map<String, Map<String, Double>> allProperties = new HashMap<String, Map<String, Double>>();
		try {

			JSONObject json = (JSONObject) parser.parse(output1);

			JSONArray characters = (JSONArray) json.get("ned");
			Iterator i = characters.iterator();

			while (i.hasNext()) {
				JSONObject mainObject = (JSONObject) i.next();
				JSONArray types = (JSONArray) mainObject.get("properties");
				Iterator iTypes = types.iterator();
				String var = "";

				while (iTypes.hasNext()) {
					Map<String, Double> urlsAndScore = new HashMap<String, Double>();
					JSONObject tempObject = (JSONObject) iTypes.next();
					String urls = (String) tempObject.get("value");
					double score = (double) tempObject.get("score");
					var = (String) tempObject.get("var");
					if (allProperties.size() > 0 && allProperties.containsKey(var)) {

						double tScore = (double) allProperties.get(var).entrySet().iterator().next().getValue();
						if (tScore < score) {

							allProperties.get(var).remove(allProperties.get(var).entrySet().iterator().next().getKey());
							allProperties.get(var).put(urls, score);
							System.out.println("var: " + var + "url : " + urls + " , Score : " + score);
						}

					} else {
						urlsAndScore.put(urls, score);
						System.out.println("var: " + var + "url : " + urls + " , Score : " + score);
						// allUrls.put("classes", urlsAndScore);
						allProperties.put(var, urlsAndScore);
					}

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		for (String vars : allProperties.keySet()) {
			Map<String, Double> allUrls = allProperties.get(vars);
			int count = 0;
			for (String urls : allUrls.keySet()) {
				System.out.println("Inside : Literal: " + urls);
				sparql = "prefix qa: <http://www.wdaqua.eu/qa#> "
						+ "prefix oa: <http://www.w3.org/ns/openannotation/core/> "
						+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "INSERT { " + "GRAPH <" + namedGraph+ "> { " + " ?a a qa:AnnotationOfRelation . " + " ?a oa:hasTarget [ " + " a oa:SpecificClass; "
						+ " oa:hasSource <" + uriQuestion + ">; " + " ] . " + " ?a oa:hasBody <" + urls + "> ;"
						+ " oa:annotatedBy <http://okbqa.disambiguationproperty.com> ; " + " oa:AnnotatedAt ?time "
						+ "}} " + "WHERE { "
						+ "BIND (IRI(str(RAND())) AS ?a) ." + "BIND (now() as ?time) " + "}";
				logger.info("Sparql query {}", sparql);
				loadTripleStore(sparql, endpoint);
				count++;
			}
			System.out.println("Count is : "+ count);
		}
		
		
//		Map<String, Map<String, Double>> allUrls = new HashMap<String, Map<String, Double>>();
//
//		try {
//
//			JSONObject json = (JSONObject) parser.parse(output1);
//
//			JSONArray characters = (JSONArray) json.get("ned");
//			Iterator i = characters.iterator();
//
//			while (i.hasNext()) {
//				JSONObject mainObject = (JSONObject) i.next();
//				JSONArray types = (JSONArray) mainObject.get("properties");
//				Iterator iTypes = types.iterator();
//				Map<String, Double> urlsAndScore = new HashMap<String, Double>();
//
//				while (iTypes.hasNext()) {
//
//					JSONObject tempObject = (JSONObject) iTypes.next();
//					String urls = (String) tempObject.get("value");
//					double score = (double) tempObject.get("score");
//					urlsAndScore.put(urls, score);
//					System.out.println("url : " + urls + " , Score : " + score);
//
//				}
//
//				allUrls.put("properties", urlsAndScore);
//
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		for (String urls : allUrls.get("properties").keySet()) {
//			System.out.println("Inside : Literal: " + allUrls.get("properties").get(urls));
//			sparql = "prefix qa: <http://www.wdaqua.eu/qa#> "
//					+ "prefix oa: <http://www.w3.org/ns/openannotation/core/> "
//					+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "INSERT { " + "GRAPH <" + namedGraph + "> { "
//					+ "  ?a a qa:AnnotationOfSpotProperty . " + "  ?a oa:hasTarget [ "
//					+ "           a    oa:SpecificProperty; " + "           oa:hasSource    <" + uriQuestion + ">; "
//					+ "  ] . " + "  ?a oa:hasBody <" + urls + "> ;"
//					+ "     oa:annotatedBy <http://agdistis.aksw.org> ; " + "	    oa:AnnotatedAt ?time  "
//					+ "     oa:score \"" + allUrls.get("properties").get(urls) + "\"ˆˆxsd:decimal ." + "}} "
//					+ "WHERE { " + "BIND (IRI(str(RAND())) AS ?a) ." + "BIND (now() as ?time) " + "}";
//			logger.info("Sparql query {}", sparql);
//			loadTripleStore(sparql, endpoint);
//		}

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
}
