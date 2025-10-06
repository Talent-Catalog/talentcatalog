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


import spacy

nlp = spacy.load("en_core_web_sm")
doc = nlp("I like green eggs and ham. John works at Talent Beyond Boundaries in Australia")
for token in doc:
    print(token.text, token.pos_, token.dep_)

print("===")

for ent in doc.ents:
    print(ent.text, ent.label_)

print("===")

doc1 = nlp("I hate football")
doc2 = nlp("I don't like sport")
print (doc1.similarity(doc2))
