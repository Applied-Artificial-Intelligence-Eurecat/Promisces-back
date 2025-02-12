import datetime
import json
import sys
import time


from flask import Flask, request
from rdkit import Chem
from rdkit.Chem import AllChem, DataStructs, rdchem
import psycopg2

conn = psycopg2.connect(
    dbname="promisces",
    user="postgres",
    password="root",
    host="promisces-postgres",
    port="5432"
)

app = Flask(__name__)


def parse_smiles(smiles):
    try:
        mol = Chem.MolFromSmiles(smiles)
        if mol is None:
            raise ValueError("SMILES string could not be parsed into a valid molecule.")
        return mol
    except (rdchem.KekulizeException, ValueError) as e:
        # print(f"Error parsing SMILES '{smiles}': {e}")
        return None


def fetch_data_paginated(cursor, query, page_size=50):
    offset = 0
    while True:
        paginated_query = f"{query} LIMIT {page_size} OFFSET {offset}"
        cursor.execute(paginated_query)
        rows = cursor.fetchall()
        if not rows:  # Stop when no more data is returned
            break
        yield rows  # Return the current batch of rows
        offset += page_size


def parse_string_to_dict(row_numericaldata):
    d = {}
    for item in row_numericaldata.split(";"):
        key, value = item.replace("|", "\t").split("\t")
        d[key] = float(value)
    return d

def calculate_most_similar(og_smiles, substance_name):
    similarities = []  # (nomsustancia, similitud)
    cur = conn.cursor()

    start = datetime.datetime.now()

    query_molecule = parse_smiles(og_smiles)
    if query_molecule is None:
        return []
    query_fp = AllChem.GetMorganFingerprintAsBitVect(query_molecule, 2, nBits=1024)

    query = """SELECT * FROM substance ORDER BY id"""
    data_gen = fetch_data_paginated(cur, query)
    while (datetime.datetime.now() - start).seconds < 30:
        page = next(data_gen, None)
        if page is None:
            break
        for row in page:
            try:
                row_name = row[12]
                row_smiles = row[5]
                row_numericaldata = row[14]
            except IndexError:
                continue

            if row_name == substance_name:
                continue

            row_mol = parse_smiles(row_smiles)
            if row_mol is None:
                continue
            row_fp = AllChem.GetMorganFingerprintAsBitVect(row_mol, 2, nBits=1024)

            similarity_score = DataStructs.TanimotoSimilarity(query_fp, row_fp)

            if len(similarities) < 50:
                numdata = parse_string_to_dict(row_numericaldata)

                similarities.append((row_name, similarity_score, numdata.get('P_score_average', 0.0), numdata.get('M_score_average', 0.0), numdata.get('B_score_average', 0.0), numdata.get('ExposureScore_Water_KEMI', 0.0)))
                similarities = sorted(similarities, key=lambda x: x[1], reverse=True)

            elif similarity_score > similarities[-1][1]:
                numdata = parse_string_to_dict(row_numericaldata)

                similarities[-1] = (row_name, similarity_score, numdata.get('P_score_average', 0.0), numdata.get('M_score_average', 0.0), numdata.get('B_score_average', 0.0), numdata.get('ExposureScore_Water_KEMI', 0.0))
                similarities = sorted(similarities, key=lambda x: x[1], reverse=True)

    cur.close()

    return similarities


@app.route('/comparation', methods=['POST'])
def compare():
    request_data = str(request.data.decode("ascii", errors="ignore"))
    r_data = json.loads(request_data)

    result = json.dumps(calculate_most_similar(r_data["smiles"], r_data["substance_name"]))

    return result


if __name__ in "__main__":
    # app.logger.disabled = True
    app.run(host='0.0.0.0')
