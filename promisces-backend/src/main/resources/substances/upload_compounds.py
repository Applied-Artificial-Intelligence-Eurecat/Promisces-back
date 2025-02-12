import csv
import json

import requests
from time import sleep

# url = "http://promiscesapi:8080/substances/"
url = "http://localhost:7070/substances/"

separate_keys = [
    'CAS No (Neutral form)',
    'EC No.',
    'Promisces ID',
    'Norman SusDat ID',
    'Canonical SMILES',
    'Full Name',
    'UBA List (Priority Group)',
    'Priority Group definition',
    'CLP harmonised'
]

with open('echa_sectors.json', 'r') as json_echa:
    echa_sectors = json.loads(json_echa.read())

# sleep(30)
# Substituir read per url de totes les substancies (tots els casn)
with open('WF2.csv', 'r', errors='ignore') as csvfile:
    reader = csv.DictReader(csvfile, delimiter=';')
    for row in reader:
        # Numerical data
        numerical_data = {}
        for key in reader.fieldnames:
            try:
                if "SU" not in key:
                    numerical_data[key] = float(row[key].replace(",", "."))
            except (ValueError, TypeError):
                pass

        # CERoutes
        routes_result = {}
        for route in "abcde":
            routes_result[f"route_{route}"] = False
            for key in reader.fieldnames:
                if f"route_{route}" in key:
                    if row[key] == "CERT":
                        routes_result[f"route_{route}"] = True
        routes_message = []
        for k in routes_result.keys():
            if routes_result[k]:
                routes_message.append({
                    "name": k
                })

        # ECHA Sectors of use
        sector_message = []
        for key in reader.fieldnames:
            if key.startswith("SU"):
                if int(row[key]) == 1:
                    sector_message.append({
                        "code": key,
                        "name": echa_sectors[key]
                    })

        classes = row['chemical_class'].replace("[", "").replace("]", "").replace("'", "")
        chemical_classes_message = [c.strip() for c in classes.split(",")]
        print(chemical_classes_message)

        substance = {
            'casNumber': row['cas_no'],
            'ECNumber': row['ec_no'],
            'promiscesID': row['promisces_id'],
            'normanSusDatID': row['norman_susdat_id'],
            'canonicalSMILES': row['canonical_smiles'],
            'nameSynonyms': [
                row['full_name']
            ],
            'group': {
                'groupName': row['uba_list'],
                'groupDescription': ''
            },
            'persistence': numerical_data.get('P_average_value', 0.0),
            'mobility': numerical_data.get('M_average_value', 0.0),
            'toxicity': numerical_data.get('T_score_score', 0.0),
            'potentialEnvironmentEmissions': '',
            'addressedApplication': '',
            'CLPharmonised': "True" if row['T_score_clp_harmonised'] == "CERT" else "False",
            'numericalData': numerical_data,
            'conservativeClassification': row['conservative_classification'],
            'robustClassification': row['robust_classification'],
            'averageClassification': row['average_classification'],
            'ceRoutes': routes_message,
            'sectorsOfUse': sector_message,
            'chemicalClasses': chemical_classes_message
        }
        r = requests.post(url, json=substance)
        if r.status_code != 200:
            if "error" in r.headers.keys():
                print(r.status_code, r.headers["error"])
            else:
                print(r.status_code)
            if r.status_code == 500:
                break
        else:
            print("OK")
