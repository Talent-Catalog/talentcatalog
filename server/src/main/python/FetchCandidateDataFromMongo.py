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
import csv
from bson import json_util
import sys
from pymongo import MongoClient


"""
FetchCandidateDataFromMongo.py

Prerequisites
-------------
1. Create & activate a virtualenv, then install PyMongo:

   $ python3 -m venv venv
   $ source venv/bin/activate
   $ pip install pymongo

2. Configure the connection settings in `main()`

3. Run the script:

   $ python FetchCandidateDataFromMongo.py > docs.json 2> missing.log
"""


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
      print(json_util.dumps(doc), end='', flush=True)
    else:
      print(f"Missing document for publicId: {pub_id}", file=sys.stderr)
  print(']', flush=True)


def main():
  # file_path = 'List11741Candidates.csv'  # Input file with UUIDs (one per line)
  file_path = 'sample.csv'
  mongo_uri = 'mongodb://tctalent:tctalent@localhost:27018/'
  db_name = 'tctalent'
  collection_name = 'candidates'

  client = MongoClient(mongo_uri)
  collection = client[db_name][collection_name]

  stream_documents_to_stdout(file_path, collection)


if __name__ == '__main__':
  main()
