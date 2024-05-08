#  Copyright (c) 2024 Talent Beyond Boundaries.
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

from elasticsearch import Elasticsearch

from elasticsearch_utils import delete_index, create_alias

# Define the alias and the index names
index_to_be_deleted = "candidates"
index_to_be_aliased = "candidates_v1"
alias_name = "candidates"

# Connect to elastic search
es = Elasticsearch(["http://localhost:9200"])
# es = Elasticsearch(cloud_id="Your_Cloud_ID")


if __name__ == "__main__":
    # Delete the old index
    delete_index(es, index_to_be_deleted)

    # Create an alias for the new index
    create_alias(es, index_to_be_aliased, alias_name)
