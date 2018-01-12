import json
from SPARQLWrapper import SPARQLWrapper, JSON


sparql = SPARQLWrapper('http://dbpedia.org/sparql')

with open('unofficial_update_final_dataset.json', 'r') as f:
     data = json.load(f)

output = []
for i in data:
    answers = []
    sparql.setQuery(i['sparql_query'])
    sparql.setReturnFormat(JSON)
    results = sparql.query().convert()
    print(i['verbalized_question'])
    print(i['sparql_query'])
    print(results)
    if 'results' in results.keys():
        for result in results['results']['bindings']:
            if 'uri' in result.keys():
                answers.append(result['uri']['value'])
            elif 'callret-0' in result.keys():
                answers.append(result['callret-0']['value'])
    elif 'boolean' in results.keys():
        answers.append(results['boolean'])

    output.append({'_id': i['_id'],
                   'question': i['corrected_question'],
                   'sparql_query': i['sparql_query'],
                   'sparql_answers': answers})

with open('questions_answers.json', 'w') as ofile:
    json.dump(output, ofile)
