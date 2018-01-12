package eu.wdaqua.qanary.querybuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
public class QueryBuilder extends QanaryComponent {
	private static final Logger logger = LoggerFactory.getLogger(QueryBuilder.class);

	/**
	 * implement this method encapsulating the functionality of your Qanary
	 * component
	 * 
	 * @throws Exception
	 */
	@Override
	public QanaryMessage process(QanaryMessage myQanaryMessage) throws Exception {
		logger.info("process: {}", myQanaryMessage);
		String detectedPattern = "";

		List<String> classes = new ArrayList<String>();
		List<String> properties = new ArrayList<String>();
		List<String> entities = new ArrayList<String>();

		QanaryUtils myQanaryUtils = this.getUtils(myQanaryMessage);
		QanaryQuestion<String> myQanaryQuestion = this.getQanaryQuestion(myQanaryMessage);
		String myQuestion = myQanaryQuestion.getTextualRepresentation();
		logger.info("Question: {}", myQuestion);

		// entities

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
				+ " ?a oa:hasBody ?uri . " + "} " + "ORDER BY ?start ";

		ResultSet r = myQanaryUtils.selectFromTripleStore(sparql);
		String argument = "";
		while (r.hasNext()) {
			QuerySolution s = r.next();
			entities.add(s.getResource("uri").getURI());
			logger.info("uri info {}", s.getResource("uri").getURI());
		}

		// property
		sparql = "PREFIX qa: <http://www.wdaqua.eu/qa#> " + "PREFIX oa: <http://www.w3.org/ns/openannotation/core/> "
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "//
				+ "SELECT  ?relationurl " + "FROM <" + myQanaryQuestion.getInGraph() + "> " //
				+ "WHERE { " //
				+ "  ?a a qa:AnnotationOfRelation . " + "  ?a oa:hasTarget [ " + "           a    oa:SpecificResource; "
				+ "           oa:hasSource    <" + myQanaryQuestion.getUri() + ">. " + "  ] ; "
				+ "     oa:hasBody ?relationurl ." + "} " //
				+ "ORDER BY ?start ";

		r = myQanaryUtils.selectFromTripleStore(sparql);

		while (r.hasNext()) {
			QuerySolution s = r.next();
			properties.add(s.getResource("uri").getURI());
			logger.info("uri info {}", s.getResource("uri").getURI());
		}

		// classes
		sparql = "PREFIX qa: <http://www.wdaqua.eu/qa#> " + "PREFIX oa: <http://www.w3.org/ns/openannotation/core/> "
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "//
				+ "SELECT ?uri " + "FROM <" + myQanaryQuestion.getInGraph() + "> " //
				+ "WHERE { " //
				+ "    ?a a qa:AnnotationOfClass . " + "?a oa:hasTarget [ "
				+ "		     a               oa:SpecificClass. " //
				+ "		     ] " //
				+ "    ] . " //
				+ " ?a oa:hasBody ?uri . " + "} " + "ORDER BY ?start ";

		r = myQanaryUtils.selectFromTripleStore(sparql);

		while (r.hasNext()) {
			QuerySolution s = r.next();
			classes.add(s.getResource("uri").getURI());
			logger.info("uri info {}", s.getResource("uri").getURI());
		}

		String generatedQuery = "";
		if (classes.size() == 0) {

			if (properties.size() == 1) {
				if (entities.size() == 1) {
					generatedQuery = "SELECT DISTINCT ?uri WHERE { <" + entities.get(0) + "> <" + properties.get(0)
							+ "> ?uri }";
				}
				if (entities.size() == 2) {
					generatedQuery = "ASK WHERE { <" + entities.get(0) + "> <" + properties.get(0) + "> <"
							+ entities.get(1) + "> }";
				}
			} else if (properties.size() == 2) {
				if (entities.size() == 1) {
					generatedQuery = "SELECT DISTINCT ?uri WHERE { ?x <" + properties.get(0) + "> <" + entities.get(0)
							+ "> . ?x <" + properties.get(1) + "> ?uri .OPTIONAL { <" + entities.get(0) +"> <" + properties.get(0) + "> ?x. } }";
				}
				if (entities.size() == 2) {
					generatedQuery = "SELECT DISTINCT ?uri WHERE { ?uri <" + properties.get(0) + "> <" + entities.get(0)
							+ "> . ?uri <" + properties.get(1) + "> <" + entities.get(1) + "> . }";
				}
			}

		} else if (classes.size() == 1) {

			if (properties.size() == 0) {
				if (entities.size() == 1) {

				}
				if (entities.size() == 2) {

				}
			} else if (properties.size() == 1) {
				if (entities.size() == 1) {
					generatedQuery = "SELECT DISTINCT ?uri WHERE {?uri <" + properties.get(0) + "> <" + entities.get(0)
							+ "> . ?uri <https://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + classes.get(0) + ">}";
				}
				if (entities.size() == 2) {

				}
			} else if (properties.size() == 2) {
				if (entities.size() == 1) {
					generatedQuery = "SELECT DISTINCT ?uri WHERE { ?x <" + properties.get(0) + "> <" + entities.get(0)
							+ "> . ?x <" + properties.get(1)
							+ "> ?uri . ?x <https://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + classes.get(0)
							+ ">}";
				}
				if (entities.size() == 2) {
					generatedQuery = "SELECT DISTINCT ?uri WHERE {?uri <" + properties.get(0) + "> <" + entities.get(0)
							+ "> . ?uri <" + properties.get(1) + "> <" + entities.get(1)
							+ "> . ?uri <https://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + classes.get(0) + ">}";
				}
			}

		} else if (classes.size() == 2) {

			if (properties.size() == 0) {
				if (entities.size() == 1) {

				}
				if (entities.size() == 2) {

				}
			} else if (properties.size() == 1) {
				if (entities.size() == 1) {

				}
				if (entities.size() == 2) {

				}
			} else if (properties.size() == 2) {
				if (entities.size() == 1) {

				}
				if (entities.size() == 2) {

				}
			}
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
}
