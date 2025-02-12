import csv

import requests as r

with open("data/entities/substances.csv") as f:
    reader = csv.DictReader(f)
    for row in reader:
        row_data = {}
        for key in reader.fieldnames:
            if not row[key] == "":
                if key == "nameSynonyms":
                    row_data[key] = row[key].split(";")
                else:
                    row_data[key] = row[key]
        res = r.post("http://localhost:8080/substances/", json=row_data)
        if res.status_code == 200:
            print("OK")
        else:
            print(res.status_code)

# De moment son el mateix, quan canviin s'haurà de modificar
for entity in ["strategies", "sectors", "routes"]:
    with open(f"data/entities/{entity}.csv") as f:
        reader = csv.DictReader(f)
        for row in reader:
            row_data = {}
            for key in reader.fieldnames:
                if not row[key] == "":
                    row_data[key] = row[key]
            res = r.post(f"http://localhost:8080/{entity}/", json=row_data)
            if res.status_code == 200:
                print("OK")
            else:
                print(res.status_code)

with open("data/entities/solutions.csv") as f:
    reader = csv.DictReader(f)
    for row in reader:
        row_data = {}
        for key in reader.fieldnames:
            if not row[key] == "":
                if key in "technologyReadinessLevel,capacity,cost,mobilisationYield,degradationYield,degradationCompleteness":
                    if "criteria" not in row_data:
                        row_data["criteria"] = {}
                    row_data["criteria"][key] = row[key]
                if key == "type":
                    if "criteria" not in row_data:
                        row_data["criteria"] = {}
                    row_data["criteria"][key] = row[key]
                    row_data[key] = row[key]
                else:
                    row_data[key] = row[key]
        res = r.post("http://localhost:8080/solutions/", json=row_data)
        if res.status_code == 200:
            print("OK")
        else:
            print(res.status_code)


def upload_link(owner, recipient):
    with open(f"data/links/{owner}{recipient}.csv") as f:
        reader = csv.DictReader(f)
        for row in reader:
            row_data = {}
            for key in reader.fieldnames:
                if not row[key] == "":
                    row_data[key] = row[key]
            res = r.post(f"http://localhost:8080/{owner}/link/{recipient}/", json=row_data)
            if res.status_code == 200:
                print("OK")
            else:
                print(res.status_code)


upload_link("solutions", "routes")
upload_link("solutions", "strategies")
upload_link("substances", "sectors")
upload_link("substances", "solutions")
