#!/usr/bin/env python3
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
for key in servers:
    lines = []
    lines.append(header + '\n')
    lines.append('docker stop $(docker ps -a -q) \n')
    lines.append('docker rm $(docker ps -a -q) \n')
    
    lines.append('echo "Starting stardog container"')
    lines.append('docker run -itd -v /data/qanary:/stardog-4.1.1/qanary -p 5820:4000 --net="host" --name stardog qanary/stardog \n')
        
    lines.append('echo "Starting qapipeline container"')
    lines.append('docker run -itd -p 8080:5000 --net="host" --name qapipeline qanary/qa.pipeline \n')
    
    lines.append('sleep 10 \n')
    
    lines.append('echo "Creating qanary triple store"')
    lines.append('docker exec -it stardog /bin/bash -c "./bin/stardog-admin db create -n qanary; exit" \n')
    
    
    lines.append('echo "Starting services" \n')
    for c in servers[key]:
        lines.append('docker run -d -P --net="host" --name ' + c.lower() + ' -t qanary/' + c.lower())
        
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
