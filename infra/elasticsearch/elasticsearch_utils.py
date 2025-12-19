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

import configparser
import os
from elasticsearch import Elasticsearch
from getpass import getpass

# Configuration File Path
es_config = './config.ini'


def connect_to_es():
    return load_es_config(es_config)


def load_es_config(config_path):
    config = configparser.ConfigParser()
    print("Current Working Directory:", os.getcwd())
    if os.path.exists(config_path):
        config.read(config_path)
        cloud_id = config.get('Elasticsearch', 'cloud_id', fallback=None)
        if cloud_id:
            es = Elasticsearch(
                cloud_id=cloud_id,
            )
            if not es.ping():
                print("Enter credentials for Elastic Cloud ID: ", cloud_id)
                username, password = get_credentials()
                es = Elasticsearch(
                    cloud_id=cloud_id,
                    http_auth=(username, password)
                )
            if es.ping():
                print("Connected to Elasticsearch: ", es)
                return es
            else:
                print("Failed to connect to Elasticsearch. Exiting.")
                exit(1)
        else:
            # Default to localhost if cloud_id is not provided
            print("Connecting to Elasticsearch at localhost")
            return Elasticsearch(["http://localhost:9200"])
    else:
        # Default to localhost if configuration file does not exist
        print("Connecting to Elasticsearch at localhost")
        return Elasticsearch(["http://localhost:9200"])


def get_credentials():
    username = input("Enter your Elasticsearch username: ")
    password = getpass("Enter your Elasticsearch password: ")
    return username, password


def create_index_if_not_exists(es, index_name, mappings):
    """
    Creates an index in Elasticsearch with the specified mappings if it does not
    already exist.
    """
    if not es.indices.exists(index=index_name):
        es.indices.create(index=index_name, body=mappings)
        print(f"Index created: '{index_name}'.")
    else:
        print(f"Index already exists: '{index_name}'.")


def reindex_data(es, old_index_name, new_index_name):
    """
    Re-indexes data from one Elasticsearch index to another within the same
    Elasticsearch instance.
    """
    reindex_body = {
        "source": {"index": old_index_name},
        "dest": {"index": new_index_name}
    }
    print(f"Re-indexing from '{old_index_name}' to '{new_index_name}'...")
    es.reindex(body=reindex_body, wait_for_completion=True)
    print(f"Data re-indexed from '{old_index_name}' to '{new_index_name}'.")


def reindex_data_with_full_names(es, old_index_name, new_index_name):
    """
    Re-indexes data from one Elasticsearch index to another within the same
    Elasticsearch instance.
    """
    reindex_body = {
        "source": {"index": old_index_name},
        "dest": {"index": new_index_name},
        "script": {
            "source": "ctx._source.fullName = ctx._source.firstName + ' ' "
                      "+ ctx._source.lastName"
        }
    }
    print(f"Re-indexing from '{old_index_name}' to '{new_index_name}'...")
    es.reindex(body=reindex_body, wait_for_completion=True)
    print(f"Data re-indexed from '{old_index_name}' to '{new_index_name}'.")


def delete_index(es, index_name):
    """
    Deletes an index from Elasticsearch if it exists.
    """
    try:
        if es.indices.exists(index=index_name):
            es.indices.delete(index=index_name)
            print(f"Index '{index_name}' successfully deleted.")
        else:
            print(f"Index '{index_name}' does not exist.")
    except Exception as e:
        print(f"An error occurred while deleting index '{index_name}': {e}")


def create_alias(es, index_name, alias_name):
    """
    Creates an alias for the specified index if the alias does not already
    exist.
    """
    try:
        if not es.indices.exists_alias(name=alias_name):
            alias_body = {
                "actions": [
                    {"add": {"index": index_name, "alias": alias_name}}
                ]
            }
            es.indices.update_aliases(body=alias_body)
            print(f"Alias '{alias_name}' created for index '{index_name}'.")
        else:
            print(f"Alias '{alias_name}' already exists.")
    except Exception as e:
        print(f"An error occurred creating alias: {e}")


def update_alias(es, old_index_name, new_index_name, alias_name):
    """
    Updates an alias by removing an old index and adding a new index to the
    alias.
    """
    try:
        if es.indices.exists_alias(name=alias_name):
            alias_body = {
                "actions": [
                    {"remove": {"index": old_index_name, "alias": alias_name}},
                    {"add": {"index": new_index_name, "alias": alias_name}}
                ]
            }
            es.indices.update_aliases(body=alias_body)
            print(f"Alias '{alias_name}' updated: Removed '{old_index_name}' "
                  f"and added '{new_index_name}'.")
        else:
            print(f"Alias '{alias_name}' does not exist. No action performed.")
    except Exception as e:
        print(f"An error occurred updating alias: {e}")
