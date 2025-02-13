#  Copyright (c) 2024 Talent Catalog.
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

from elasticsearch_utils import create_index_if_not_exists, connect_to_es

# Define the index names
index_name = "candidates_v3"


# Define the mappings and settings for the new index
mappings = {
  "mappings": {
    "properties": {
      "_class": {"type": "keyword", "index": False, "doc_values": False},
      "additionalInfo": {"type": "text"},
      "candidateNumber": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "certifications": {"type": "text"},
      "city": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "country": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "cvs": {"type": "text"},
      "dob": {"type": "date", "format": "basic_date"},
      "drivingLicense": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "educationMajors": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "educations": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "email": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "externalId": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "firstName": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "fullIntakeCompletedDate": {"type": "long"},
      "fullName": {"type" : "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "gender": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "id": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "ieltsScore": {"type": "double"},
      "englishAssessmentScoreDet": {"type": "long"},
      "jobExperiences": {"type": "text"},
      "lastName": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "maritalStatus": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "masterId": {"type": "long"},
      "maxEducationLevel": {"type": "long"},
      "migrationOccupation": {"type": "text"},
      "minEnglishSpokenLevel": {"type": "long"},
      "minEnglishWrittenLevel": {"type": "long"},
      "miniIntakeCompletedDate": {"type": "long"},
      "nationality": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "numberDependants": {"type": "long"},
      "occupations": {
        "type": "nested",
        "properties": {
          "_class": {"type": "keyword", "index": False, "doc_values": False},
          "name": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
          "yearsExperience": {"type": "long"}}},
      "otherLanguages": {
        "type": "nested",
        "properties": {
          "_class": {"type": "keyword", "index": False, "doc_values": False},
          "minSpokenLevel": {"type": "integer"},
          "minWrittenLevel": {"type": "integer"},
          "name": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}
        }
      },
      "partner": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "phone": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "regoReferrerParam": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "residenceStatus": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "skills": {"type": "text"},
      "state": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "status": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "surveyType": {"type": "long"},
      "unhcrStatus": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}},
      "updated": {"type": "long"}
    }
  }
}


if __name__ == "__main__":
    es = connect_to_es()
    create_index_if_not_exists(es, index_name, mappings)
