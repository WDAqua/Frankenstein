import csv
import os
import errno


ifilename = 'all-components.csv'
servers = {}
components = set()
filenum = 1
with open(ifilename) as csvfile:
    reader = csv.DictReader(csvfile)
    for row in reader:
        if (row['NED'] == row['Relation Linker'] == row['Class Identifier'] == row['Query Builder'] == ''):
            servers[filenum] = components
            components = set()
            filenum += 1
        else:
            components.update({row['NED'], row['Relation Linker'], row['Class Identifier'], row['Query Builder']})

header = '#!/bin/bash'
homefolder = 'QANARY=/qanarySetup/Applications/workspace/qanary_qa'
stardogfolder = 'STARDOG=/qanarySetup/Applications/startdog/stardog-4.1.3'
logfolder = 'QANARY_LOG=/qanarySetup/Applications/workspace/qanary_logs'
for key in servers:
    lines = []
    lines.append(header + '\n')
    lines.append(stardogfolder)
    lines.append(homefolder)
    lines.append(logfolder + '\n')
    lines.append('# Start stardog')
    lines.append('$STARDOG/bin/stardog-admin server start\n')
    lines.append('# Start qa pipeline and qa components')
    lines.append('rm $STARDOG/system.lock')
    lines.append('nohup java -jar $QANARY/qanary_pipeline-template/target/qa.pipeline-1.1.0.jar &')
    lines.append('sleep 10')
    for c in servers[key]:
        lines.append('nohup java -jar ' + '$QANARY/' + c + '/target/' + c + '-1.0.0.jar'+ ' 2>'+'$QANARY_LOG/'+ c+'.errorlog'+' 1>'+ '$QANARY_LOG/'+c+'.outlog' +' &')

    filename = 'servers/server' + str(key) + '.sh'
    if not os.path.exists(os.path.dirname(filename)):
        try:
            os.makedirs(os.path.dirname(filename))
        except OSError as exc:
            if exc.errno == errno.EEXIST and os.path.isdir(path):
                pass
            else: raise

    with open(filename, 'w') as file:
        for line in lines:
            file.write(line + '\n')
        file.close()
