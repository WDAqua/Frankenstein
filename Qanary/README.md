![](https://raw.githubusercontent.com/WDAqua/Qanary/master/doc/logo-qanary_s.png)

# Frankenstein reusable QA components integrated together following the Qanary Methodology
## Qanary in a Nutshell

Qanary is a Methodology for Creating Question Answering Systems it is part of the [WDAqua project](http://wdaqua.informatik.uni-bonn.de) where question answering systems are researched and developed. Here, we are providing our key contributions on-top of the [RDF vocabulary qa](https://github.com/WDAqua/QAOntology) the reference implementation of the Qanary methodology. This repository contributes several sub-resources for Question Answring Community to build knowledge driven QA systems incorporating a standard [RDF vocabulary qa](https://github.com/WDAqua/QAOntology) integrated with Frankenstein framrwork. All the resources are reusable. In brief, the following sub-projects are available all aiming at establishing an ecosystem for question answering systems.

 * [**Qanary Pipeline**](#qanarypipeline) implementation: a central component where components for question answering systems are connected automatically and can be called by Web UIs
 * Qanary component implementations: components providing wrappers to existing functionality or implement new question answering approaches
   * a [**Qanary component template**](#qanarycomponenttemplate) implementation: use this to build you own component ([source](    
 https://github.com/WDAqua/Qanary/wiki)) as it provides [several features](https://github.com/WDAqua/Qanary/wiki/Frequently-Asked-Questions)
    * a [**Qanary component XXX**](#qanarycomponenttemplate) Each folder with Qanary component prefix is an independent QA component. The detailed list of QA components can be seen in Component_List.csv file.

## In a Nutshell: How to run the Qanary components required for the Frankenstein approach 

### Step 1: Checkout the Qanary framework

```
git clone https://github.com/WDAqua/Qanary
```

### Step 2: Build the Qanary framework
Switch to the new repository folder and execute the command
```
mvn -DskipDockerBuild install
```
or (if you like to skip the Docker image creation, then) use
```
mvn -DskipDockerBuild install
```
Now the Qanary components are locally installed. 

### Step 3: Run the Qanary QA System 
Start your triplestore. Run the Qanary pipeline component in the folder ``qanary_pipeline-template`` (JAR or Docker container).

### Step 4: Build the Qanary Components in the Frankenstein folder
Switch to the folder ``Frankenstein/Qanary`` and execute the command
```
mvn -DskipDockerBuild install
```
or (if you like to skip the Docker image creation, then) use
```
mvn -DskipDockerBuild install
```
Now, all components are available. You can start the ones you would like to test (or all of them). They will register themself to the running pipeline component. 

*Note:* A longer description on how to create a QA pipeline with Qanary is available [here](https://github.com/WDAqua/Qanary/wiki/Demo:-How-to-Create-a-Question-Answering-System-capable-of-Analyzing-the-Question-%22What-is-the-real-name-of-Batman%3F%22).


<a name="qanarypipeline"></a>
## Qanary Pipeline

[source](https://github.com/WDAqua/Qanary/tree/master/qanary_pipeline-template)

Qanary pipeline is the central component for using Qanary infrastructure. It registered each component as RESTful service and components can be run independently.


<a name="qanarycomponents"></a>
## Qanary Components


<a name="qanarycomponenttemplate"></a>
### Qanary component template
 Do you have excellant research which you want to include in Frankenstein? To add a new component in Frankenstein infrastructure in this repository, please see [source](https://github.com/WDAqua/Qanary/wiki)
 

<a name="qanarylcevaluator"></a>
### Qanary LC-Evaluator
Qanary LC-Evaluator is the evaluation component for evaluating any component present in Frankenstein Infrstructure.
[source](https://github.com/WDAqua/Frankenstein/tree/master/Qanary/lcevaluator). The user need to change in two files. Go to resource folder in lcevaluator folder. Change in pipeline.csv and update the name of the component you want to execute. If you want to run a complete pipeline, please include components in a single line saperated by comma. In questions.csv file, update the list of questions/text you want to analyse or annotate using the Frankenstein components. Please assign unique ID to each question and your structure of each line in question.csv looks like: "QuestionID, your question text".

## Additional Resources


<a name="qaldevaluator"></a>
### QALD evaluator
[source](https://github.com/WDAqua/Qanary/tree/master/qald-evaluator)

This component is to evaluate the components with QALD benchmark.


<a name="qaldnerddataset"></a>
### Automatic Execution of QA pipelines and Components
[source](https://github.com/WDAqua/Frankenstein/tree/master/Qanary/scripts/server-bash)
In this sub-folder, Bash scripts are given to run the component, use these scripts. Go to the folder, there are saperate instruction to use these scripts as part of Frankenstein Evaluation resources.



## Publications / References

If you want to inform yourself about the Qanary methodology in general, please use this publication:  *Andreas Both, Dennis Diefenbach, Kuldeep Signh, Saedeeh Shekarpour, Didier Cherix and Christoph Lange: Qanary - A Methodology for Vocabulary-driven Open Question Answering Systems* appearing in [13th Extended Semantic Web Conference](http://2016.eswc-conferences.org), 2016.


## Stuff used to make this:

 * [Spring Boot](http://projects.spring.io/spring-boot/) project

## How to run the code
For just running Frankenstein components using Qanary, here are detailed instutructions: 

### Without docker

 * Clone the GitHub repository: `git clone https://github.com/WDAqua/Frankenstein`

 * Install Java 8 (see <http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html> for details)

 * Install maven (see <https://maven.apache.org/install.html> for details)

 * Compile and package your project using maven: `mvn clean install -DskipDockerBuild`
   The _install_ goal will compile, test, and package your project’s code and then copy it into the local dependency repository.

 * Install Stardog Triplestore (<http://stardog.com/>) and start it in background. Create a database with the name _qanary_. All the triples generated by the components will be stored in the _qanary_ database.

 * Run the pipeline component:
   ```
   cd qanary_pipeline-template/target/
   java -jar target/qa.pipeline-<version>.jar
   ```
 * After `maven build` jar files will be generated in the corresponding folders of the Qanary components. For example, to start the Alchemy API components:
   ```
   cd qanary_component-Alchemy-NERD
   java -jar target/qa.Alchemy-NERD-0.1.0.jar
   ```
 
 * After running corresponding jar files, you can see Springboot application running on <http://localhost:8080/#/overview> that will tell the status of currently running components.

 * Now your pipeline is ready to use. Go to <http://localhost:8080/startquestionansweringwithtextquestion>. Here you can find a User Interface to interact for adding question via web interface, and then select the components you need to include in the pipeline via checking a checkbox for each component. Press the start button and you are ready to go!

### With docker

 * Clone the GitHub repository: `git clone https://github.com/WDAqua/Qanary`

 * Install Java 8 (see <http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html> for details)

 * Install maven (see <https://maven.apache.org/install.html> for details)
 
 * Install docker (see <https://docs.docker.com/engine/installation/> for details)
 
 * Start docker service (see <https://docs.docker.com/engine/admin/> for details)

 * Compile and package your project using maven: `mvn clean install`
   The _install_ goal will compile, test, and package your project’s code and then copy it into the local dependency repository. Additionally, it will generate docker images for each component that will be stored in your local repository.

 * Configure the script `start.sh` according to the services you want to start. Each service runs inside a docker instance. At least the docker containers `stardog`, `pipeline` and one qanary component have to be up and running.
 Afterwards, run the script `initdb.sh` that creates the database _qanary_ in the stardog triple store.
 
 * After executing the run script, you can see Springboot application running on <http://localhost:8080/#/overview> that will tell the status of currently running components.

 * Now your pipeline is ready to use. Go to <http://localhost:8080/startquestionansweringwithtextquestion>. Here you can find a User Interface to interact for adding question via web interface, and then select the components you need to include in the pipeline via checking a checkbox for each component. Press the start button and you are ready to go!
