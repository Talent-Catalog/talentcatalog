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

Each script's purpose and sequence within the indexing lifetime are immediately clear, allowing for 
systematic updates, or rollbacks if necessary.

## Configuration: config.ini

The `config.ini` file contains environment-specific configuration details for connecting to the 
Elasticsearch cluster. This file allows you to easily switch between different environments, such as 
staging and production, by specifying the correct cloud_id for each. If no cloud_id is specified, the 
scripts will default to connecting to a local Elasticsearch instance.

A cloud_id is a unique identifier for your Elasticsearch deployment. It encodes the region and 
specific instance details for a deployment on Elastic cloud, allowing the Python scripts to connect 
to the right cluster.

### Example Configuration

```ini
[Elasticsearch]
; Elasticsearch configuration
; Specify the cloud_id for the environment you are connecting to.
; cloud_id is the unique identifier for your Elasticsearch deployment.
;
; For staging, use the cloud_id provided by your Elastic Cloud deployment.
; For production, use the production cloud_id.
;
; Example:
; cloud_id = tc-staging:xxxxxxxx

cloud_id = 

; If no cloud_id is provided, the default behavior is to connect to 
; Elasticsearch on localhost at http://localhost:9200.
; Otherwise, a connection will be attempted to the configured Elasticsearch 
; deployment for the provided cloud_id.
```

- **When a cloud_id is provided**: The scripts will attempt to connect to the Elasticsearch 
deployment (e.g., staging or production) corresponding to the cloud_id specified.

- **When no cloud_id is provided**: If the cloud_id field is left empty or commented out, the
default behavior is to connect to an Elasticsearch instance running locally at http://localhost:9200.


## Setting Up the Python Environment
Before executing the scripts in this directory, ensure that your Python environment is correctly set
up. Follow these steps to install Python and the required Python packages.

### Install Python
If Python is not already installed on your system, you can download and install it from the official 
[Python website](https://www.python.org/downloads/) or use a package manager for your operating 
system. For macOS, you can install Python using Homebrew:

```zsh
brew install python3
```

This will install Python 3 and its package manager, pip3, which you will use to install Python
packages.

### Setting Up a Virtual Environment
It is recommended to use a virtual environment for Python projects to avoid conflicts between
package versions. You can create and activate a virtual environment named `tc_env`  in your project 
directory as follows:

```zsh
cd path/to/your/project/talentcatalog/infra/elasticsearch
python3 -m venv tc_env
````

Activate the virtual environment:

On macOS and Linux:

```zsh
source tc_env/bin/activate
```

On Windows:
```dos
venv\Scripts\activate
```

### Install Required Python Packages
The scripts in this directory require the Elasticsearch Python client. To install the necessary 
Python packages, use the pip3 command:

```zsh
pip3 install elasticsearch
```

### Verify Installation
Check that the installation of the Elasticsearch client was successful by running the following 
command, which should print the version number of the Elasticsearch package:

```zsh
python3 -c "import elasticsearch; print(elasticsearch.__version__)"

> (8, 13, 1)
```

