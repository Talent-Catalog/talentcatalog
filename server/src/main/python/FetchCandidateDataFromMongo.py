#  Copyright (c) 2025 Talent Catalog.
#
#  This program is free software: you can redistribute it and/or modify it under
#  the terms of the GNU Affero General Public License as published by the Free
#  Software Foundation, either version 3 of the License, or any later version.
#
#  This program is distributed in the hope that it will be useful, but WITHOUT
#  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
#  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
#  for more details.
#
#  You should have received a copy of the GNU Affero General Public License
#  along with this program. If not, see https://www.gnu.org/licenses/.


"""
FetchCandidateDataFromMongo.py

Prerequisites
-------------
1. Create & activate a virtualenv, then install PyMongo:

   $ python3 -m venv venv
   $ source venv/bin/activate
   $ pip install pymongo requests

2. Configure the connection settings in `main()`

3. Run the script:

   $ python3 FetchCandidateDataFromMongo.py > candidates.json 2> missing_ids.log
"""


import csv
import sys
import requests
from pymongo import MongoClient
from bson import json_util
from pymongo.errors import ServerSelectionTimeoutError, OperationFailure


def login_to_tc(base_url: str, username: str, password: str) -> str:
  """
  POST to /auth/login to retrieve a JWT.
  Returns the full Authorization header value, e.g. "Bearer eyJ..."
  """
  url = f"{base_url}/auth/login"
  payload = {"username": username, "password": password}
  resp = requests.post(url, json=payload)
  resp.raise_for_status()

  data = resp.json()
  token_type = data.get("tokenType", "Bearer")
  access_token = data["accessToken"]
  return f"{token_type} {access_token}"


def generate_public_ids_file(
    base_url: str,
    auth_header: str,
    public_list_id: str,
    output_csv: str
) -> None:
  """
  GET /public/{publicListId}/public-ids using the JWT bearer token,
  then write the returned IDs (one per line) into output_csv with a header.
  """
  url = f"{base_url}/saved-list-candidate/public/{public_list_id}/public-ids"
  headers = {"Authorization": auth_header}
  resp = requests.get(url, headers=headers)
  resp.raise_for_status()

  public_ids = resp.json()  # should be a list or set of strings

  with open(output_csv, "w", newline="") as f:
    writer = csv.writer(f)
    writer.writerow(["publicId"])  # header (will be skipped by read_public_ids)
    for pid in public_ids:
      writer.writerow([pid])


def read_public_ids(file_path):
  """Yield the public_id from each row of a CSV with header."""
  with open(file_path, newline='') as f:
    reader = csv.reader(f)
    next(reader)                       # skip header row
    for row in reader:
      if row and row[0].strip():
        yield row[0].strip()


def stream_documents_to_stdout(file_path, collection):
  print('[', end='', flush=True)
  first = True
  for pub_id in read_public_ids(file_path):
    doc = collection.find_one({'publicId': pub_id})
    if doc:
      if not first:
        print(',', end='', flush=True)
      else:
        first = False
      print(json_util.dumps(doc, indent=2), end='', flush=True)
    else:
      print(f"Missing document for publicId: {pub_id}", file=sys.stderr)
  print(']', flush=True)


def main():
  # ┌───────────────────────────────────────────────────────────────────────────┐
  # │              CONFIGURE TC SERVICE & LIST ID TO PULL                       │
  # └───────────────────────────────────────────────────────────────────────────┘
  tc_base_url    = "http://localhost:8080/api/admin"
  tc_username    = "appAnonDatabaseService"
  tc_password    = "12345678"
  public_list_id = "a2JqM7-7SQ-uy7pZnX0S8w"

  # ┌───────────────────────────────────────────────────────────────────────────┐
  # │           FETCH & SAVE THE PUBLIC IDs CSV FOR MONGO LOOKUP                │
  # └───────────────────────────────────────────────────────────────────────────┘
  ids_csv = "sample.csv"
  auth_hdr = login_to_tc(tc_base_url, tc_username, tc_password)
  generate_public_ids_file(tc_base_url, auth_hdr, public_list_id, ids_csv)

  # ┌───────────────────────────────────────────────────────────────────────────┐
  # │              CONFIGURE MONGO DB CONNECTION                                │
  # └───────────────────────────────────────────────────────────────────────────┘
  mongo_uri = 'mongodb://tctalent:tctalent@localhost:27018/'
  db_name = 'tctalent'
  collection_name = 'candidates'

  # ┌───────────────────────────────────────────────────────────────────────────┐
  # │                NOW CONNECT TO MONGO & STREAM DOCUMENTS                    │
  # └───────────────────────────────────────────────────────────────────────────┘
  client = MongoClient(mongo_uri)
  try:
    client.admin.command('ping')
  except ServerSelectionTimeoutError as e:
    print("Could not connect to MongoDB:", e, file=sys.stderr)
    sys.exit(1)
  except OperationFailure as e:
    print("Authentication failed:", e, file=sys.stderr)
    sys.exit(1)

  collection = client[db_name][collection_name]
  stream_documents_to_stdout(ids_csv, collection)


if __name__ == '__main__':
  main()
