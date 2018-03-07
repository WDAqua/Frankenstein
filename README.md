# Welcome to the Frankenstein! 
This repository contain all the reusable resources present in Frankenstein Architecture, and detailed description of how to use the resources within Frankenstein. The Frankenstein Architecture currently contain 29 components. For detailed list of the components, their high level input and output, please refer to [Component List](https://github.com/WDAqua/Frankenstein/blob/master/Component%20List.csv). This repository is maintained uder GPL 3.0 lisence agreement. The repository contain two folders:
## Qanary 
In this folder, all the 29 integrated components within Frankenstein is given as independent resources. The detailed instruction for how to run these components,
how to include a newer component can be seen in a saperate read me file within the folder. 

## Learning
In this folder you will see Frankenstein learning module that learns from given features and f-score to select the best 
component per task. This folder also contain another folder named Dataset, this folder contain our detailed results, 
empirical study results etc from original Frankenstein paper 
##### " Singh, Kuldeep et al. Why Reinvent the Wheel- Lets Build Question Answering Systems Together. in proceedings of The Web Conference (WWW 2018), to appear. [Link](https://www.researchgate.net/publication/322057242_Why_Reinvent_the_Wheel-Let%27s_Build_Question_Answering_Systems_Together).

# 380 QA pipelines in Frankenstein
These 29 indegrated components given in Component_List.csv can be used to build QA pipelines. We have 11 Named entity
recognition components (NER), 9 Named entity Disambiguation (NED), 5 Relation Linker( RL), 2 Class Linker (CL), and 2 Query Builder(QB).
We do not have benchmark for NER, and AGDISTIS NED tool need question and recognised entities as input so all 11 NER components are 
used for creating input for AGDISTIS. Hence, logically it become 19 NED component, can be combined with 5 RL, 2 Cl, and 2 QB 
components, resulting into 380 QA pipelines (19X5X2X2)

