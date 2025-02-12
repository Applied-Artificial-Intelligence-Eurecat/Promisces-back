import csv
import json
from time import sleep

import requests
from tqdm import tqdm

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

matrix_id_to_name = {
    3: 'Surface water - River water',
    4: 'Surface water - Lake water',
    5: 'Surface water - Transitional water',
    6: 'Surface water - Coastal water',
    7: 'Surface water - Territorial (marine) water',
    8: 'Surface water - Reservoirs',
    9: 'Surface water - Other',
    10: 'Ground water',
    11: 'Waste water - Urban',
    12: 'Waste water - Industrial',
    13: 'Waste water - Municipal',
    14: 'Waste water - Other',
    16: 'Sediments - River water',
    17: 'Sediments - Lake water',
    18: 'Sediments - Transitional water',
    19: 'Sediments - Coastal water',
    20: 'Sediments - Territorial (marine) water',
    22: 'Sediments - Other',
    24: 'Suspended matter - River water',
    27: 'Suspended matter - Coastal water',
    28: 'Suspended matter - Territorial (marine) water',
    30: 'Suspended matter - Other',
    32: 'Sewage sludge - Industrial',
    33: 'Sewage sludge - Municipal',
    34: 'Sewage sludge - Other',
    36: 'Soil - Provincial',
    37: 'Soil - Municipal',
    38: 'Soil - Other',
    39: 'Biota - Sea',
    40: 'Biota - River water',
    41: 'Biota - Lake water',
    42: 'Biota - Transitional water',
    43: 'Biota - Coastal water',
    44: 'Biota - Territorial (marine) water',
    46: 'Biota - Terrestrial',
    47: 'Biota - Other',
    55: 'Air - Indoor air - Other',
    57: 'Air - Ambient air - Urban',
    61: 'Air - Ambient air - Background',
    64: 'Air - Workplace air (industrial) - Indoor',
    70: 'Outdoor air - Gas & particle phases'
}

with open('echa_sectors.json', 'r') as json_echa:
    echa_sectors = json.loads(json_echa.read())

with open("InchiKeysAll.txt", "r") as f:
    inchikeys = [l.rstrip() for l in f.readlines()]

# sleep(30)
i = 0
with open("DSF_V5.csv", "r", errors="ignore") as f:
    reader = csv.DictReader(f, delimiter=';')
    for row in reader:
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
                    if row[key] != "False" and row[key] != "":
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

        ikey = row['StdInChIKey']
        if ikey in inchikeys:
            inchikeys.remove(ikey)

        d = requests.get(f"https://www.norman-network.com/nds/api/empodat/inchikey/{ikey}/JSON")

        if d.status_code != 200:
            print(d.status_code, d.text)
            break

        empodat_data = json.loads(d.text)

        samples = []
        concentrationValues = {}
        if "Data" in empodat_data.keys():
            for occurence in empodat_data["Data"]:
                try:
                    sample = {}
                    try:
                        matrix_name = occurence["Sample matrix"]
                        sample['concentration'] = occurence["Concentration"]["Value"]
                        sample['samplingDate'] = occurence["Sampling date"]
                        sample['limitOfDetection'] = float(
                            occurence['QA/QC information about chemical data']['Limit of detection']['value'])
                        sample['limitOfQuantification'] = float(
                            occurence['QA/QC information about chemical data']['Limit of quantification']['value'])
                        sample['stationName'] = occurence['Station name']
                    except (KeyError, TypeError) as e:
                        continue
                    if len(matrix_name.split(" - ")) == 2:
                        sample['sampleMatrixType'] = matrix_name.split(" - ")[0]
                        sample['sampleMatrix'] = matrix_name.split(" - ")[1]
                    else:
                        sample['sampleMatrixType'] = matrix_name
                        sample['sampleMatrix'] = matrix_name
                    samples.append(sample)
                except Exception:
                    pass

                try:
                    matrix_name = occurence["Sample matrix"]
                    con_value = occurence["Concentration"]["Value"]
                except (KeyError, TypeError) as e:
                    continue
                if matrix_name in concentrationValues.keys():
                    concentrationValues[matrix_name] = concentrationValues[matrix_name] + [con_value]
                else:
                    concentrationValues[matrix_name] = [con_value]

        substance = {
            'casNumber': row['CAS_RN_Dashboard'],
            'ECNumber': '',
            'promiscesID': row['promisces_id'],
            'normanSusDatID': row['Norman_SusDat_ID'],
            'canonicalSMILES': row['SMILES_Dashboard'],
            'inchikey': row['StdInChIKey'],
            'nameSynonyms': [
                row['Name']
            ],
            'group': {
                'groupName': row['uba_list'],
                'groupDescription': ''
            },
            'persistence': numerical_data.get('P_score_average', 0.0),
            'mobility': numerical_data.get('M_score_average', 0.0),
            'toxicity': numerical_data.get('B_score_average', 0.0),
            'potentialEnvironmentEmissions': '',
            'addressedApplication': '',
            'CLPharmonised': "False",
            'numericalData': numerical_data,
            'conservativeClassification': row['PMT_class_conservative'],
            'robustClassification': row['PMT_class_robust'],
            'averageClassification': row['PMT_class_average'],
            'ceRoutes': routes_message,
            'sectorsOfUse': sector_message,
            'chemicalClasses': chemical_classes_message,
            'concentrationValues': concentrationValues,
            'samples': samples,
            'tClass': 1 if "T" in row['t_class'] else 0
        }
        r = requests.post(url, json=substance)
        if r.status_code != 200:
            if "error" in r.headers.keys():
                print(i, r.status_code, r.headers["error"])
            else:
                print(i, r.status_code)
            if r.status_code == 500:
                pass
        else:
            print(i, "OK")
            i += 1

print("Inchi")
for ikey in tqdm(inchikeys):
    d = requests.get(f"https://www.norman-network.com/nds/api/susdat/inchikey/{ikey}/JSON")

    if d.status_code != 200:
        print(d.status_code, d.text)
        break

    susdat_data = json.loads(d.text)

    d = requests.get(f"https://www.norman-network.com/nds/api/empodat/inchikey/{ikey}/JSON")

    if d.status_code != 200:
        print(d.status_code, d.text)
        break

    empodat_data = json.loads(d.text)

    samples = []
    concentrationValues = {}
    if "Data" in empodat_data.keys():
        for occurence in empodat_data["Data"]:
            try:
                sample = {}
                try:
                    matrix_name = occurence["Sample matrix"]
                    sample['concentration'] = occurence["Concentration"]["Value"]
                    sample['samplingDate'] = occurence["Sampling date"]
                    sample['limitOfDetection'] = float(
                        occurence['QA/QC information about chemical data']['Limit of detection']['value'])
                    sample['limitOfQuantification'] = float(
                        occurence['QA/QC information about chemical data']['Limit of quantification']['value'])
                    sample['stationName'] = occurence['Station name']
                except (KeyError, TypeError) as e:
                    continue
                if len(matrix_name.split(" - ")) == 2:
                    sample['sampleMatrixType'] = matrix_name.split(" - ")[0]
                    sample['sampleMatrix'] = matrix_name.split(" - ")[1]
                else:
                    sample['sampleMatrixType'] = matrix_name
                    sample['sampleMatrix'] = matrix_name
                samples.append(sample)
            except Exception:
                pass

            try:
                matrix_name = occurence["Sample matrix"]
                con_value = occurence["Concentration"]["Value"]
            except (KeyError, TypeError) as e:
                continue
            if matrix_name in concentrationValues.keys():
                concentrationValues[matrix_name] = concentrationValues[matrix_name] + [con_value]
            else:
                concentrationValues[matrix_name] = [con_value]

    try:
        substance = {
            'casNumber': susdat_data['sus_cas'][len("CAS_RN: "):],
            'ECNumber': '',
            'promiscesID': '',
            'normanSusDatID': susdat_data['sus_id'],
            'canonicalSMILES': susdat_data['SMILES'],
            'inchikey': ikey,
            'nameSynonyms': [
                susdat_data['sus_name']
            ],
            'group': {
                'groupName': '-',
                'groupDescription': ''
            },
            'persistence': 0.0,
            'mobility': 0.0,
            'toxicity': 0.0,
            'potentialEnvironmentEmissions': '',
            'addressedApplication': '',
            'CLPharmonised': "False",
            'numericalData': {'value': 0.0},
            'conservativeClassification': '',
            'robustClassification': '',
            'averageClassification': '',
            'ceRoutes': [],
            'sectorsOfUse': [],
            'chemicalClasses': [],
            'concentrationValues': concentrationValues,
            'samples': samples,
            'tClass': 1 if "T" in row['t_class'] else 0
        }
    except KeyError:
        continue
    r = requests.post(url, json=substance)
    if r.status_code != 200:
        if r.status_code == 500:
            pass
    else:
        i += 1
        print(i)
