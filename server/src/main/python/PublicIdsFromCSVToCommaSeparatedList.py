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
import os

def extract_public_ids_from_csv(csv_file_path):
  base, _ = os.path.splitext(csv_file_path)
  txt_path = base + ".txt"

  values = []
  with open(csv_file_path, 'r') as csv_file:
    csv_reader = csv.reader(csv_file)
    for row in csv_reader:
      if len(row) > 1:
        if row[0].isdigit():
          values.append(f"'{row[1].strip()}'")

  result = ",".join(values)

  with open(txt_path, 'w', encoding="utf-8") as txt_file:
    txt_file.write(result)

  print(f"Public IDs extracted from {csv_file_path} and saved to {txt_path}")

if __name__ == "__main__":
  csv_file = input("Enter the path to the CSV file: ").strip()
  extract_public_ids_from_csv(csv_file)
