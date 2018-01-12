#!/bin/bash

STARDOG=/qanarySetup/Applications/startdog/stardog-4.1.3
QANARY=/qanarySetup/Applications/workspace/qanary_qa
QANARY_LOG=/qanarySetup/Applications/workspace/qanary_logs

# Start stardog
$STARDOG/bin/stardog-admin server start

# Start qa pipeline and qa components
rm $STARDOG/system.lock
nohup java -jar $QANARY/qanary_pipeline-template/target/qa.pipeline-1.1.0.jar &
sleep 10
nohup java -jar $QANARY/qanary_component-QB-Sina/target/qanary_component-QB-Sina-1.0.0.jar 2>$QANARY_LOG/qanary_component-QB-Sina.errorlog 1>$QANARY_LOG/qanary_component-QB-Sina.outlog &
nohup java -jar $QANARY/qa.qanary_component-DiambiguationClass-OKBQA/target/qa.qanary_component-DiambiguationClass-OKBQA-1.0.0.jar 2>$QANARY_LOG/qa.qanary_component-DiambiguationClass-OKBQA.errorlog 1>$QANARY_LOG/qa.qanary_component-DiambiguationClass-OKBQA.outlog &
nohup java -jar $QANARY/qa.qanary_component-AnnotationofSpotClass/target/qa.qanary_component-AnnotationofSpotClass-1.0.0.jar 2>$QANARY_LOG/qa.qanary_component-AnnotationofSpotClass.errorlog 1>$QANARY_LOG/qa.qanary_component-AnnotationofSpotClass.outlog &
nohup java -jar $QANARY/qa.qanary_component-AnnotationofSpotProperty-tgm/target/qa.qanary_component-AnnotationofSpotProperty-tgm-1.0.0.jar 2>$QANARY_LOG/qa.qanary_component-AnnotationofSpotProperty-tgm.errorlog 1>$QANARY_LOG/qa.qanary_component-AnnotationofSpotProperty-tgm.outlog &
nohup java -jar $QANARY/qanary_component-REL-ReMatch/target/qanary_component-REL-ReMatch-1.0.0.jar 2>$QANARY_LOG/qanary_component-REL-ReMatch.errorlog 1>$QANARY_LOG/qanary_component-REL-ReMatch.outlog &
nohup java -jar $QANARY/qanary_component-REL-RelationLinker/target/qanary_component-REL-RelationLinker-1.0.0.jar 2>$QANARY_LOG/qanary_component-REL-RelationLinker.errorlog 1>$QANARY_LOG/qanary_component-REL-RelationLinker.outlog &
nohup java -jar $QANARY/qa.qanary_component-QueryBuilder/target/qa.qanary_component-QueryBuilder-1.0.0.jar 2>$QANARY_LOG/qa.qanary_component-QueryBuilder.errorlog 1>$QANARY_LOG/qa.qanary_component-QueryBuilder.outlog &
nohup java -jar $QANARY/qanary_component-NED-tagme/target/qanary_component-NED-tagme-1.0.0.jar 2>$QANARY_LOG/qanary_component-NED-tagme.errorlog 1>$QANARY_LOG/qanary_component-NED-tagme.outlog &
nohup java -jar $QANARY/qanary_component-REL-RelationLinker2/target/qanary_component-REL-RelationLinker2-1.0.0.jar 2>$QANARY_LOG/qanary_component-REL-RelationLinker2.errorlog 1>$QANARY_LOG/qanary_component-REL-RelationLinker2.outlog &
nohup java -jar $QANARY/qa.qanary_component-DiambiguationProperty-OKBQA/target/qa.qanary_component-DiambiguationProperty-OKBQA-1.0.0.jar 2>$QANARY_LOG/qa.qanary_component-DiambiguationProperty-OKBQA.errorlog 1>$QANARY_LOG/qa.qanary_component-DiambiguationProperty-OKBQA.outlog &
