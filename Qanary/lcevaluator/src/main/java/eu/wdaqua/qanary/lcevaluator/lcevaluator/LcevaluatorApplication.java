package eu.wdaqua.qanary.lcevaluator.lcevaluator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.opencsv.CSVReader;

@SpringBootApplication
public class LcevaluatorApplication {
	private static final Logger logger = LoggerFactory.getLogger(LcevaluatorApplication.class);

	@Value("${lcevaluator.questions.csvfile}")
	private String csvFileQuestions;

	@Value("${lcevaluator.pipelines.csvfile}")
	private String csvFilePipelines;

	@Value("${lcevaluator.export.directory}")
	private String directoryForGraphDumps; // ends with slash

	@Value("${lcevaluator.stardog.executables.dir}")
	private String stardogExecutablesDir;

	@Value("${lcevaluator.qanary.endpoint}")
	private String uriServer;

	@Value("${lcevaluator.pipelines.startindex}")
	private int startindex;

	@Value("${lcevaluator.pipelines.range}")
	private int range;

	@PostConstruct
	public void process() throws IOException, JSONException {

		logger.warn("run here from index {} the next {} pipeline configurations", startindex, range);

		// get all pipelines and questions
		List<List<String>> configurations = getAllAvailablePipelines();
		List<List<String>> questions = getAvailableQuestions();

		// for all selected pipelines: execute the question and dump the
		// collected data
		logger.warn("for all questions and all configuraitons within the given range {}:{}", startindex, range);
		for (int i = startindex; i < startindex + range; i++) {
			String pipelineNumber = configurations.get(i).get(0);
			String pipeline = configurations.get(i).get(1);
			logger.info("{}. for: {} --> {}", i, pipelineNumber, pipeline);

			// for all questions
			int questionCounter = 0;
			for (List<String> question : questions) {

				// call the pipeline and get the namedGraph
				String questionId = question.get(0);
				String questionText = question.get(1);
				logger.info("{}. question: {}", questionCounter, questionText);

				// response from qanary pipeline server
				String response = callQanaryPipeline(pipeline, questionText);

				// Retrieve the computed uris
				JSONObject responseJson = new JSONObject(response);
				String endpoint = responseJson.getString("endpoint");
				String namedGraph = responseJson.getString("outGraph");

				// dump the data
				String exportFilename = directoryForGraphDumps + "dump_" + pipelineNumber + "_" + questionId + ".ttl";
				dumpGraphAndDeleteGraph(namedGraph, exportFilename);

				questionCounter++;
			}

		}

		logger.info("process for all questions and selected pipeline configurtions finished");

	}

	private void dumpGraphAndDeleteGraph(String namedGraph, String exportFilename) throws IOException {
		// shell command (linux)
		String commandDump = stardogExecutablesDir + "stardog data export -g " + namedGraph + " --format TURTLE qanary "
				+ exportFilename + "";
		String commandDelete = stardogExecutablesDir + "stardog data remove -g " + namedGraph + " qanary ";

		logger.info("dump via: {}", commandDump);
		executeCommandOnShellAndLogOutput(commandDump);

		logger.info("remove via: {}", commandDelete);
		executeCommandOnShellAndLogOutput(commandDelete);

	}

	/**
	 * run the dump and remove command on Stardog
	 * 
	 * @param commandDump
	 * @throws IOException
	 */
	private void executeCommandOnShellAndLogOutput(String commandDump) throws IOException {
		Process proc = Runtime.getRuntime().exec(commandDump);

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		// read the output from the command
		logger.debug("Here is the standard output of the command:");
		String s = null;
		while ((s = stdInput.readLine()) != null) {
			logger.warn(s);
		}

		// read any errors from the attempted command
		logger.debug("Here is the standard error of the command (if any):");
		while ((s = stdError.readLine()) != null) {
			logger.warn(s);
		}
	}

	/**
	 * call to Qanary restful service endpoint to start a pipeline run for the
	 * given question and given pipeline configuration
	 * 
	 * @param pipeline
	 * @param questionText
	 * @return
	 */
	private String callQanaryPipeline(String pipeline, String questionText) {
		RestTemplate restTemplate = new RestTemplate();
		UriComponentsBuilder service = UriComponentsBuilder.fromHttpUrl(uriServer);

		MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<String, String>();
		bodyMap.add("question", questionText);
		bodyMap.add("componentlist[]", pipeline);
		String response = restTemplate.postForObject(service.build().encode().toUri(), bodyMap, String.class);
		logger.info("Response pipline: {}", response);
		return response;
	}

	/**
	 * get all questions (from prepared CSV file)
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<List<String>> getAvailableQuestions() throws IOException {

		List<List<String>> questions = new ArrayList<>();

		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(csvFileQuestions));
			String[] line;
			while ((line = reader.readNext()) != null) {
				logger.debug("Questions [id={} , question={} ]", line[0], line[1]);
				List<String> question = new LinkedList<String>();
				question.add(line[0]);
				question.add(line[1]);
				questions.add(question);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return questions;
	}

	/**
	 * get all available pipelines (from prepared CSV file)
	 * 
	 * @return
	 */
	private List<List<String>> getAllAvailablePipelines() {

		// this gives you a 2-dimensional array of strings
		List<List<String>> configurations = new ArrayList<>();

		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(csvFilePipelines));
			String[] line;
			while ((line = reader.readNext()) != null) {
				logger.debug("Pipelines [id={} , pipeline={} ]", line[0], line[1]);
				List<String> configuration = new LinkedList<String>();
				configuration.add(line[0]);
				configuration.add(line[1]);
				configurations.add(configuration);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return configurations;
	}

	public static void main(String[] args) {
		//SpringApplication.run(LcevaluatorApplication.class, args);
		SpringApplication.run(LcevaluatorApplication.class, args);
	}
}
