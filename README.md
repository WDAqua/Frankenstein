# Welcome to the Frankenstein! 
This repository contain all the reusable resources present in Frankenstein Architecture, and detailed description of how to use the resources within Frankenstein. This repository is maintained uder GPL 3.0 lisence agreement and submitted as resource track paper in ESWC 2018. Please see lisence.md for details. The repository contain two
folders (Please not, we have merged components describe in ESWC paper as evaluation components (Resource R2) in Qanary folder based on User's feedback for better ease of use. In original paper, the evaluation components supposed to be in Evaluation Folder). The two folders are:
## Qanary 
In this folder, all the 29 integrated components within Frankenstein is given as independent resources. The detailed instruction for how to run these components,
how to include a newer component can be seen in a saperate read me file within the folder. 


## Learning
In this folder you will see Frankenstein learning module that learns from given features and f-score to select the best 
component per task. This folder also contain another folder named Dataset, this folder contain our detailed results, 
empirical study results etc from original Frankenstein paper 
##### " Singh, Kuldeep et al. Why Reinvent the Wheel- Lets Build Question Answering Systems Together. in proceedings of The Web Conference (WWW 2018), to appear.

# 380 QA pipelines in Frankenstein
These 29 indegrated components given in Component_List.csv can be used to build QA pipelines. We have 11 Named entity
recognition components (NER), 9 Named entity Disambiguation (NED), 5 Relation Linker( RL), 2 Class Linker (CL), and 2 Query Builder(QB).
We do not have benchmark for NER, and AGDISTIS NED tool need question and recognised entities as input so all 11 NER components are 
used for creating input for AGDISTIS. Hence, logically it become 19 NED component, can be combined with 5 RL, 2 Cl, and 2 QB 
components, resulting into 380 QA pipelines (19X5X2X2)

