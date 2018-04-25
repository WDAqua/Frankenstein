# Welcome to the Frankenstein! 
This repository contain all the reusable resources present in Frankenstein Architecture, and detailed description of how to use the resources within Frankenstein. The Frankenstein Architecture currently contain 29 components. For detailed list of the components, their high level input and output, please refer to [Component List](https://github.com/WDAqua/Frankenstein/blob/master/Component%20List.csv). This repository is maintained uder GPL 3.0 lisence agreement. The repository contain two folders:

## [Qanary](https://github.com/WDAqua/Frankenstein/tree/master/Qanary) 
In this folder, all the 29 integrated components (written using the [Qanary framework for Question Answering Systems](https://github.com/WDAqua/Qanary)) within Frankenstein are given as independent resources. 
The detailed instruction for how to run these components, how to include a newer component can be seen in the separate read me file within the folder. 

## [Learning](https://github.com/WDAqua/Frankenstein/tree/master/Learning)
In this folder, you will see the Frankenstein learning module. It learns from given features and f-score to select the best 
component per QA task (i.e., a question). This folder also contain another folder named [Dataset](https://github.com/WDAqua/Frankenstein/tree/master/Learning/Datasets), this folder contain our detailed results, 
empirical study results etc. from the original Frankenstein paper 
##### "Why Reinvent the Wheel - Let's Build Question Answering Systems Together." Singh, Kuldeep et al., in Proceedings of The Web Conference (WWW 2018), to appear. [Link](https://www.researchgate.net/publication/322057242_Why_Reinvent_the_Wheel-Let%27s_Build_Question_Answering_Systems_Together).

# 380 QA pipelines in Frankenstein
These 29 indegrated components given in ``Component_List.csv`` can be used to build QA pipelines. 
We have 11 commponents dedicated to Named Entity Recognition (NER), 9 for Named Entity Disambiguation (NED), 5 for Relation Linker (RL), 2 Class Linker (CL), and 2 Query Builder (QB).
We do not have a benchmark for NER, and AGDISTIS NED tool needs question and recognised entities as input so all 11 NER components are used for creating input for AGDISTIS. 
Hence, logically it become 19 NED components which can be combined with 5 RL, 2 Cl, and 2 QB 
components, resulting into 380 QA pipelines (19X5X2X2).

