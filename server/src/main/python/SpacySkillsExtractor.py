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
SpacySkillsExtractor.py

Proof of concept for using spaCy to extract skills from any text.

The intention is that this code will be wrapped in a REST API service which
can be called from our Spring code, passing in some text and getting back
a list of skills.

Dependencies:

* pip install spacy
* python -m spacy download en_core_web_sm

"""

import spacy
from spacy.matcher import PhraseMatcher

# --- spaCy: load base model
nlp = spacy.load("en_core_web_sm")

#Example: this should be populated from skills on our Postgres database
# populated from ESCO.
esco_skill_labels = [
  "Java", "Spring Boot", "Python", "FastAPI", "Docker", "Kubernetes", "PostgreSQL",
  "MapStruct", "Angular", "AWS", "Terraform", "Natural language processing","Spring"
]

# Matching is case-insensitive - see https://spacy.io/api/phrasematcher#init
matcher = PhraseMatcher(nlp.vocab, attr="LOWER")

# Convert text skills into an array of NLP docs.
patterns = [nlp.make_doc(s) for s in esco_skill_labels]

# Configure the matcher to recognize the skills as a special kind of match.
# See https://spacy.io/api/phrasematcher#add
matcher.add("SKILL", patterns)

# --- Example data
text = """
Senior Software Engineer at Talent Beyond Boundaries (2021–present).
Built microservices with Java, Spring Boot, and PostgreSQL on AWS (ECS Fargate).
Led FastAPI service for NLP classification using Python. Docker, Kubernetes, Terraform.
Integrated Angular front-end; used MapStruct and Lombok in Spring.
"""

doc = nlp(text)

# Run matcher on the doc to find all the skills mentioned.
skill_matches = []
for match_id, start, end in matcher(doc):
  match = doc[start:end]
  skill_matches.append(match.text)

# De-dup skills
skills = sorted(set([s.lower() for s in skill_matches]))

for skill in skills:
  print(skill)
