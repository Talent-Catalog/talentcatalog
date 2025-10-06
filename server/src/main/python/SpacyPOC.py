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
SpacyPOC.py

Prerequisites
-------------
0. The assumption is that you have installed a virtual environment in ~venv
For Intellij, follow the instructions here: https://www.jetbrains.com/help/idea/configuring-python-sdk.html#local-python-interpreters
to specify a python interpreter running in the virtual environment.

1. Create (if necessary) & activate a virtualenv, then install PyMongo:

   $ python3 -m venv venv
   $ source venv/bin/activate
   $ pip install spacy
   $ python -m spacy download en_core_web_sm

2. Run the script:

   $ python3 SpacyPOC.py
"""



# pip install spacy sentence-transformers pgvector psycopg2-binary
# python -m spacy download en_core_web_sm

import spacy
from spacy.matcher import PhraseMatcher
from sentence_transformers import SentenceTransformer, util


# --- spaCy: load base model
nlp = spacy.load("en_core_web_sm")

# --- Example: build a skills matcher from ESCO labels (or your curated list)
esco_skill_labels = [
  "Java", "Spring Boot", "Python", "FastAPI", "Docker", "Kubernetes", "PostgreSQL",
  "MapStruct", "Angular", "AWS", "Terraform", "Natural language processing",
]
matcher = PhraseMatcher(nlp.vocab, attr="LOWER")
patterns = [nlp.make_doc(s) for s in esco_skill_labels]
matcher.add("SKILL", patterns)

def extract_chunks(text: str):
  """Return structured chunks: roles, responsibilities, skill phrases, etc."""
  doc = nlp(text)
  skill_spans = []
  for match_id, start, end in matcher(doc):
    span = doc[start:end]
    skill_spans.append(span.text)

  # Simple chunking heuristics: per sentence + include skills found
  sentences = [s.text.strip() for s in doc.sents if s.text.strip()]
  # De-dup skills
  skills = sorted(set([s.lower() for s in skill_spans]))
  return sentences, skills

# --- SBERT model (search-friendly)
embedder = SentenceTransformer("multi-qa-MiniLM-L6-cos-v1")

def embed_texts(texts):
  return embedder.encode(texts, normalize_embeddings=True)  # cosine-ready

# --- Example data
candidate_cv = """
Senior Software Engineer at Talent Beyond Boundaries (2021–present).
Built microservices with Java, Spring Boot, and PostgreSQL on AWS (ECS Fargate).
Led FastAPI service for NLP classification using Python. Docker, Kubernetes, Terraform.
Integrated Angular front-end; used MapStruct and Lombok in Spring.
"""

job_description = """
We’re hiring a backend engineer to build microservices on AWS.
Must have strong Java (Spring Boot), PostgreSQL, and Docker.
Nice-to-have: Python FastAPI, Kubernetes, Terraform, NLP experience.
"""

# --- Extract structured chunks
cv_sentences, cv_skills = extract_chunks(candidate_cv)
jd_sentences, jd_skills = extract_chunks(job_description)

# --- Compose candidate indexable units (sentences + lifted skills)
candidate_units = cv_sentences + cv_skills
job_units = [job_description] + jd_skills  # query as a whole + explicit skills

# --- Embed
cand_vecs = embed_texts(candidate_units)
job_vecs = embed_texts(job_units)

# --- Score: job → candidate (max or mean over candidate chunks)
import numpy as np
scores = util.cos_sim(job_vecs[0], cand_vecs).cpu().numpy().flatten()
top_k_idx = scores.argsort()[::-1][:5]

print("Top matching candidate chunks:")
for i in top_k_idx:
  print(f"{scores[i]:.3f} :: {candidate_units[i]}")
