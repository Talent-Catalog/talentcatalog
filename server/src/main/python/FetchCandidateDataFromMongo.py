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
import json
import sys
from pymongo import MongoClient

def read_ids_stream(file_path):
  with open(file_path, 'r') as f:
    for line in f:
      id_str = line.strip()
      if id_str:
        yield id_str

def stream_documents_to_stdout(file_path, collection):
  print('[', end='', flush=True)
  first = True
  for id_val in read_ids_stream(file_path):
    doc = collection.find_one({'_id': id_val})
    if doc:
      if not first:
        print(',', end='', flush=True)
      else:
        first = False
      print(json.dumps(doc), end='', flush=True)
    else:
      print(f"Missing document for ID: {id_val}", file=sys.stderr)
  print(']', flush=True)

def main():
  file_path = 'ids.txt'  # Input file with UUIDs (one per line)
  mongo_uri = 'mongodb://localhost:27017/'
  db_name = 'your_db'
  collection_name = 'your_collection'

  client = MongoClient(mongo_uri)
  collection = client[db_name][collection_name]

  stream_documents_to_stdout(file_path, collection)

if __name__ == '__main__':
  main()
