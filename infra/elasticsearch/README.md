# Elasticsearch Configuration

## Purpose
This directory contains scripts and utilities to manage Elasticsearch indices and environment 
settings under source control. The primary goal is to ensure that changes to Elasticsearch 
configurations, such as index creation, data reindexing, and alias management, are consistently and 
reliably applied across all development, staging, and production environments.

## Contents
The directory includes a series of Python scripts that facilitate the management of Elasticsearch 
indices and aliases. These scripts use utility functions defined in `elasticsearch_utils.py` to 
perform common Elasticsearch operations. This utility file abstracts Elasticsearch operations into 
simpler, reusable function calls that ensure operations are executed exactly across different 
environments.

### Utility Functions
- **elasticsearch_utils.py**: Contains utility functions such as creating and deleting indices, 
creating aliases, and reindexing data. This file acts as a central toolkit for manipulating 
Elasticsearch indices and aliases programmatically.

## Naming Convention
Additional files within this directory follow a structured naming convention that describes the 
version of the index they affect, the order of operation, and a brief description of their purpose:

- **v{version}_{order}_{description}.py**
  - **version**: The version of the Elasticsearch index this script is intended for.
  - **order**: The sequence number indicating the order in which this script should be executed 
               relative to other scripts for the same index version.
  - **description**: A short description of the operation or change the script performs.

### Example:

- **v2_2_reindex_from_candidate_v1_to_candidate_v2.py**
  - This script is the second (`_2`) operation for version 2 (`v2_`) of the index.
  - It handles the reindexing of data from the `candidate_v1` index to `candidate_v2`, as indicated 
    by the description.

### Further Examples
- **v1_1_create_candidate_v1_mapping.py**: Script for setting up the initial mapping for the 
`candidate_v1` index. This is the first script (order 1) applied for version 1 of the index.

- **v1_2_reindex_from_candidate_to_candidates_v1.py**: Handles the reindexing of data from an old 
`candidates` index to the newly created `candidate_v1` index. This is the second script for version 1.

- **v1_3_remove_candidates_index_and_create_candidates_alias.py**: Removes the old `candidates` 
index and creates a new `candidates` alias pointing to `candidates_v1`. This script is the third 
operation for index version 1.

- **v2_1_create_candidate_v2_index_mapping.py**: Initialises the mapping for the `candidate_v2` 
index that includes an additional mapping for full name.

- **v2_2_reindex_from_candidate_v1_to_candidate_v2.py**: Manages the migration of data from 
`candidate_v1` to `candidate_v2`, mapping first and last names to full names. It is the second 
operation for index version 2.

- **v2_3_change_candidates_alias_from_candidates_v1_to_candidates_v2.py**: Updates the `candidates` 
alias from pointing to `candidates_v1` to `candidates_v2`. It is the third operation for index 
version 2.

This structured approach ensures that each script's purpose and sequence within the indexing 
lifetime are immediately clear, allowing for systematic maintenance, updates, or rollbacks if 
necessary.
