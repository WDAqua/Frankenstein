package eu.wdaqua.qanary.sina;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.wdaqua.qanary.commons.QanaryMessage;
import eu.wdaqua.qanary.commons.QanaryQuestion;
import eu.wdaqua.qanary.commons.QanaryUtils;
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
public class SINA extends QanaryComponent {
	private static final Logger logger = LoggerFactory.getLogger(SINA.class);

	/**
	 * implement this method encapsulating the functionality of your Qanary
	 * component
	 * 
	 * @throws Exception
	 */
	@Override
	public QanaryMessage process(QanaryMessage myQanaryMessage) throws Exception {
		logger.info("process: {}", myQanaryMessage);
		// TODO: implement processing of question
		QanaryUtils myQanaryUtils = this.getUtils(myQanaryMessage);
		QanaryQuestion<String> myQanaryQuestion = new QanaryQuestion(myQanaryMessage);
		String myQuestion = myQanaryQuestion.getTextualRepresentation();
		logger.info("myQuestion: {}", myQuestion);
		//entity fetching from triplestore
		String sparql = "PREFIX qa: <http://www.wdaqua.eu/qa#> "
				+ "PREFIX oa: <http://www.w3.org/ns/openannotation/core/> "
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "//
				+ "SELECT ?start ?end ?uri " + "FROM <" + myQanaryQuestion.getInGraph() + "> " //
				+ "WHERE { " //
				+ "    ?a a qa:AnnotationOfInstance . " + "?a oa:hasTarget [ "
				+ "		     a               oa:SpecificResource; " //
				+ "		     oa:hasSource    ?q; " //
				+ "	         oa:hasSelector  [ " //
				+ "			         a        oa:TextPositionSelector ; " //
				+ "			         oa:start ?start ; " //
				+ "			         oa:end   ?end " //
				+ "		     ] " //
				+ "    ] . " //
				+ " ?a oa:hasBody ?uri ; "//
				+ "} ";
		logger.info("Entity: {}", sparql);

		ResultSet r = myQanaryUtils.selectFromTripleStore(sparql);
		String argument = "";
		while (r.hasNext()) {
			QuerySolution s = r.next();

			Entity entityTemp = new Entity();
			entityTemp.begin = s.getLiteral("start").getInt();

			entityTemp.end = s.getLiteral("end").getInt();

			entityTemp.uri = s.getResource("uri").getURI();
			argument += entityTemp.uri + ", ";
			System.out.println("Getting inside================");
			logger.info("uri:start:end info {}", entityTemp.uri + entityTemp.begin + entityTemp.end);
		}
		// relation fetch from triplestore
		sparql = "PREFIX qa: <http://www.wdaqua.eu/qa#> " + "PREFIX oa: <http://www.w3.org/ns/openannotation/core/> "
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "//
				+ "SELECT ?relationurl " + "FROM <" + myQanaryQuestion.getInGraph() + "> " //
				+ "WHERE { " //
				+ "  ?a a qa:AnnotationOfRelation . " + "  ?a oa:hasTarget [ " + "           a    oa:SpecificResource; "
				+ "           oa:hasSource    <" + myQanaryQuestion.getUri() + ">; "
				// + " oa:start ?start; " //
				// + " oa:end ?end " //
				+ "  ] ; " + "     oa:hasBody ?relationurl ;"
				// + " oa:annotatedBy <; "
				+ "	    oa:AnnotatedAt ?time  " + "} " //
				+ "ORDER BY ?start ";
		logger.info("Relation: {}", sparql);
		ResultSet r2 = myQanaryUtils.selectFromTripleStore(sparql);

		while (r2.hasNext()) {
			QuerySolution s = r2.next();

			Entity entityTemp2 = new Entity();
			// entityTemp2.begin = s.getLiteral("start").getInt();

			// entityTemp2.end = s.getLiteral("end").getInt();

			entityTemp2.uri = s.getResource("relationurl").getURI();
			argument += entityTemp2.uri + ", ";
			logger.info("uri info2 {}", entityTemp2.uri);
		}
		// now class fetching from triplestore
		sparql = "PREFIX qa: <http://www.wdaqua.eu/qa#> " + "PREFIX oa: <http://www.w3.org/ns/openannotation/core/> "
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "//
				+ "SELECT ?url " + "FROM <" + myQanaryQuestion.getInGraph() + "> " //
				+ "WHERE { " //
				+ "  ?a a qa:AnnotationOfClass . " + "  ?a oa:hasTarget [ " + "           a    oa:SpecificResource; "
				+ "           oa:hasSource    <" + myQanaryQuestion.getUri() + ">; "
				// + " oa:start ?start; " //
				// + " oa:end ?end " //
				+ "  ] ; " + "     oa:hasBody ?url ;"
				// + " oa:annotatedBy <; "
				+ "	    oa:AnnotatedAt ?time  " + "} " //
				+ "ORDER BY ?start ";

		ResultSet r3 = myQanaryUtils.selectFromTripleStore(sparql);

		while (r3.hasNext()) {
			QuerySolution s = r3.next();
			Entity entityTemp3 = new Entity();
			entityTemp3.uri = s.getResource("uri").getURI();
			argument += entityTemp3.uri + ", ";
			logger.info("uri info {}", s.getResource("uri").getURI());
		}

		logger.info("Sina Argument: {}", argument+": "+argument.length());
		logger.info("Sina Argument Count: {}",StringUtils.countMatches(argument, "dbpedia"));
		
		if(argument.length() > 2 && StringUtils.countMatches(argument, "dbpedia") <=3 ) {
			argument = argument.substring(0, argument.length() - 2);

			ProcessBuilder pb = new ProcessBuilder("java", "-jar", "qanary_component-QB-Sina/src/main/resources/sina-0.0.1.jar", argument);

			File output = new File("qanary_component-QB-Sina/src/main/resources/sinaoutput.txt");

			pb.redirectOutput(output);
			Process p = pb.start();
			p.waitFor();
			p.destroy();
			String outputRetrived = "";
			String line = "";
			System.out.println("file data ===========================");
			BufferedReader br = new BufferedReader(new FileReader(new File("qanary_component-QB-Sina/src/main/resources/sinaoutput.txt")));
			while ((line = br.readLine()) != null) {
				outputRetrived += line;
				System.out.println(line);
			}
			br.close();

			System.out.println("The retrived output : " + outputRetrived);
			String ar = outputRetrived
					.substring(outputRetrived.indexOf("list of final templates:") + "list of final templates:".length());
			// logger.info("Check {}", result);
			logger.info("Result {}", ar);
			ar = ar.trim();
			ar = ar.substring(1, ar.length() - 1);
			String[] parts = ar.split(",");
			logger.info("store data in graph {}", myQanaryMessage.getValues().get(myQanaryMessage.getEndpoint()));
			// TODO: insert data in QanaryMessage.outgraph

			logger.info("apply vocabulary alignment on outgraph");
			// TODO: implement this (custom for every component)
			String sparqlPart1 = "";
			String sparqlPart2 = "";
			int x = 10;
			for (int i = 0; i < parts.length; i++) {
				sparqlPart1 += "?a" + i + " a qa:AnnotationOfAnswerSPARQL . " + "  ?a" + i + " oa:hasTarget <URIAnswer> . "
						+ "  ?a" + i + " oa:hasBody \"" + parts[i].replace("\n", " ") + "\" ;"
						+ "     oa:annotatedBy <www.wdaqua.sina> ; " + "         oa:annotatedAt ?time ; "
						+ "         qa:hasScore " + x-- + " . \n";
				sparqlPart2 += "BIND (IRI(str(RAND())) AS ?a" + i + ") . \n";
			}

			sparql = "prefix qa: <http://www.wdaqua.eu/qa#> " + "prefix oa: <http://www.w3.org/ns/openannotation/core/> "
					+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "INSERT { " + "GRAPH <"
					+ myQanaryUtils.getInGraph() + "> { " + sparqlPart1 + "}} " + "WHERE { " + sparqlPart2
					+ "BIND (IRI(str(RAND())) AS ?b) ." + "BIND (now() as ?time) " + "}";
			myQanaryUtils.updateTripleStore(sparql);
		}
		else {
			logger.info("Argument is Null {}", argument);
		}
		return myQanaryMessage;
	}
	class Entity {

		public int begin;
		public int end;
		public String namedEntity;
		public String uri;

		public void print() {
			System.out.println("Start: " + begin + "\t End: " + end + "\t Entity: " + namedEntity);
		}
	}

	private static BufferedReader getOutput(Process p) {
		return new BufferedReader(new InputStreamReader(p.getInputStream()));
	}

	private static BufferedReader getError(Process p) {
		return new BufferedReader(new InputStreamReader(p.getErrorStream()));
	}
}
