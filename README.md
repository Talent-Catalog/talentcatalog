# Talent Catalog #

## Overview ##

This is the repository for the Talent Catalog (TC), which manages data 
for refugees looking for skilled migration pathways into safe countries and employment. 
 
This repository is a "mono-repo", meaning it contains multiple sub-modules all of which 
make up the Talent Catalog system. In particular, it contains: 

- **server**: the backend module of the system providing secure API (REST) access to the 
data, stored in an SQL Database. This module is written in Java / Spring Boot.
- **candidate-portal**: the frontend module through which candidates (refugees seeking skilled 
migration) are able to register and manage their details. This is written in Angular and connects 
to the REST API endpoints under `/api/candidate` provided by the server. 
- **admin-portal**: the frontend module through which admin staff are able to view, manage and annotate 
candidate details. This is written in Angular and connects to the REST API endpoints under 
`/api/admin` provided by the server.
- **public-portal**: a module through which anyone can access publicly available data. 
This is written in Angular and connects to the REST API endpoints under 
`/api/public` provided by the server.

## Contributing ##

Contributions are very welcome. Please see 
[our contribution guidelines](CONTRIBUTING.md). 
They should be submitted as pull request.
     
## How do I get set up? ##

### Install the tools ###

>IMPORTANT NOTE:
>
>These instructions are tailored for Mac users using Intellij, as this is what we use for development.
>
>On a Mac, installing with Homebrew usually works well. eg "brew install xxx".
>
>It is also probably easier to install Java directly (or from your
development IDE - see below) rather than using brew.

Download and install the latest of the following tools.

- Homebrew - [Homebrew website](https://brew.sh)

- IntelliJ IDEA - [Intellij website](https://www.jetbrains.com/idea/download/)
  - Import standard settings and run configurations from another developer
  - In development it is best to build using Intellij rather than gradle. Change the Intellij 
  setting for "Build, Execution & Deployment" > "Build Tools" > "Gradle" to build with Intellij.

- Java 17
   - At least Java 17 is required - to support the Spring Boot Framework.
   - If you are using a recent version of Intellij the version of Java that comes with it works 
 fine except that it does not have library source code - so probably best to download a new SDK
     (which you can from inside Intellij - see Project Structure|Project|SDK).
    

- Gradle [https://gradle.org/install/](https://gradle.org/install/)
  > brew install gradle

- Node [https://nodejs.org/en/](https://nodejs.org/en/)
  
    - Note that developers should use the latest version of Node for which Intellij supports 
    Angular debugging - currently that is Node 16 (which is not the latest Node with long term 
    support LTS).
    - See [https://www.jetbrains.com/help/idea/angular.html](https://www.jetbrains.com/help/idea/angular.html) 
    and https://nodejs.org/en/about/releases/
  
  > brew install node@16
    - Note the messages from brew at the end of the install. 
  You will have to manually set up the path.  
  

- Angular CLI [https://angular.io/cli](https://angular.io/cli)
  > npm install -g @angular/cli
  - To upgrade Angular versions, see https://update.angular.io/


- Docker
    - Install Docker Desktop for Mac - 
      see [docker website](https://hub.docker.com/editions/community/docker-ce-desktop-mac/)


- Elasticsearch (for text search)
    - Install Docker image. 
      See [Elastic search website](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html)
      Just pull the image to install. See later for how to run. 
      Currently in dev we use version 7.12.0 rather than the latest because it doesn't require security 
      enabled.
    > docker pull docker.elastic.co/elasticsearch/elasticsearch:7.12.0

- Kibana (for monitoring Elasticsearch)
    - Install Docker image.
      See [Elastic search website](https://www.elastic.co/guide/en/kibana/current/docker.html)
      Just pull the image to install. See later for how to run.
    > docker pull docker.elastic.co/kibana/kibana:7.12.0

- Git - [see Git website](https://git-scm.com/downloads) - Not really necessary now with Intellij 
 which will prompt you install Git if needed


- PostgreSQL - [Postgres website](https://www.postgresql.org/download/)
  - Homebrew - see https://wiki.postgresql.org/wiki/Homebrew 
  >   brew install postgresql@14
  > 
  >   brew services restart postgresql@14

### AWS management tools ###

These tools do not need to be installed in order to get the code up and running on your development
machine. However, they are needed if you want to build the TC's AWS cloud infrastructure
from the Terraform definitions in the `infra` folder.
    
- AWS CLI - [see AWS CLI website](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)
   
Once installed, needs to be configured. Log in to your AWS account, click on user top right, 
   select Security Credentials, create access key, then download to CSV file. 
   Then, theoretically this should work
  > aws configure import --csv path-to-downloaded-file.csv
    
...but it doesn't currently (it fails saying that it is missing a User Name header). Instead,
just run this and manually copy/paste the values from the CSV file as prompted.
> aws configure

- Terraform (for setting up our AWS infrastructure)
  > brew install terraform 

Once installed, you can run Terraform from the directory containing your main Terraform
file (main.tf).
In order to populate "secret" configuration values that Terraform needs to set up as
environment variables for the TC software, you need to copy a special file `terraform.tfvars`
to that directory before running terraform. Contact TBB for a copy of that file.

Then you can run `init` (only need to do this once), and then `plan` or `apply`, as needed.

  > terraform init
  >
  > terraform plan
  > 
  > terraform apply

### Setup your local database ###

 Use the psql tool.
 > psql postgres
   
Now you will see the command line prompt =#

    CREATE DATABASE tbbtalent;
    CREATE USER tbbtalent WITH SUPERUSER PASSWORD 'tbbtalent';
    \q

Ask another developer for a recent `pg_dump` of their test database - 
matching the latest version of the code.
    
    pg_dump --file=path/to/file.sql --create --username=tbbtalent --host=localhost --port=5432


Use `psql` to import that dump file into your newly created database.

    psql -h localhost -d tbbtalent -U tbbtalent -f path/to/file.sql

### Download and edit the code ###

- Clone [the repository](https://github.com/talentbeyondboundaries/tbbtalentv2.git) to your local system
- Open the root folder in IntelliJ IDEA (it should auto detect gradle and self-configure)

### Run Elasticsearch ###

Can run from Docker desktop for Mac, or (replacing appropriate version number)...

> docker rm elasticsearch

> docker run --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.12.0

Elasticsearch will run listening on port 9200. 
You can verify this by going to [localhost:9200](http://localhost:9200) in your browser

### Run Kibana (optional) ###

Can run from Docker desktop for Mac, or (replacing appropriate version number)...

> docker rm kibana

> docker run --name kibana --link elasticsearch -p 5601:5601 docker.elastic.co/kibana/kibana:7.12.0

Kibana runs listening on port 5601. 
You can verify this by going to [localhost:5601](http://localhost:5601) in your browser 

### Run the server ###

- Some secret information such as passwords and private keys are set in 
  environment variables - including programmatic access to Talent Catalog's Amazon AWS, 
  Google and Salesforce accounts. If these environment variables are not set
  the application will fail at start up. Contact TBB if you need access to these
  "secrets". On development computers they can be stored in a tbb_secrets.txt file which you can 
  hook into your computer's start up to set the relevant environment variables. 
  For example add "source ~/tbb_secrets.txt" to .bash_profile or .zshenv
  depending on whether you are running bash or zsh.

- Create a new Run Profile for `org.tbbtalent.server.TbbTalentApplication`. 
  In the Environment Variables section of Intellij, check the 
  "Include system environment variables" checkbox.
- Run the new profile, you should see something similar to this in the logs: 
```
Started TbbTalentApplication in 2.217 seconds (JVM running for 2.99)
```
- your server will be running on port 8080 (default for Spring Boot) 
(can be overridden by setting server.port in application.yml, or Intellij Run 
  Configuration, and updating environment.ts in portals)
- To test it open a browser to [http://localhost:8080/test](http://localhost:8080/test)


### Run the Candidate Portal ###

The "Candidate Portal" is an Angular Module and can be found in the directory `tbbtalentv2\ui\candidate-portal`.

Before running, make sure all the libraries have been downloaded locally by running `npm install` from the root 
directory of the module (i.e. `tbbtalentv2\ui\candidate-portal`):

> cd tbbtalentv2\ui\candidate-portal
>
> npm install

It is also a good idea to install fsevents for MacOS which will greatly
reduce your CPU usage

> npm install fsevents
> 
> npm rebuild fsevents
 
 

Then from within the same directory run: 

> ng serve

You will see log similar to: 

```
chunk {main} main.js, main.js.map (main) 11.9 kB [initial] [rendered]
chunk {polyfills} polyfills.js, polyfills.js.map (polyfills) 236 kB [initial] [rendered]
chunk {runtime} runtime.js, runtime.js.map (runtime) 6.08 kB [entry] [rendered]
chunk {styles} styles.js, styles.js.map (styles) 16.6 kB [initial] [rendered]
chunk {vendor} vendor.js, vendor.js.map (vendor) 3.55 MB [initial] [rendered]
i ｢wdm｣: Compiled successfully.
```

The Candidate Portal is now running locally and you can open a browser (chrome preferred) to: 

[http://localhost:4200](http://localhost:4200)


__Note:__ _this is for development mode only. In production, the Candidate Portal module will be bundled 
into the server and serve through Apache Tomcat._  

### Run the Public Portal ###


The "Public Portal" is an Angular Module and can be found in the directory `tbbtalentv2\ui\public-portal`.

As for the "Candidate Portal", make sure all libraries are installed locally.

Then from within the same directory run:

> ng serve

You will see log similar to:

```
chunk {main} main.js, main.js.map (main) 11.9 kB [initial] [rendered]
chunk {polyfills} polyfills.js, polyfills.js.map (polyfills) 236 kB [initial] [rendered]
chunk {runtime} runtime.js, runtime.js.map (runtime) 6.08 kB [entry] [rendered]
chunk {styles} styles.js, styles.js.map (styles) 16.6 kB [initial] [rendered]
chunk {vendor} vendor.js, vendor.js.map (vendor) 3.55 MB [initial] [rendered]
i ｢wdm｣: Compiled successfully.
```

The Public Portal is now running locally and you can open a browser (chrome preferred) to:

[http://localhost:4202](http://localhost:4202)


__Note:__ _this is for development mode only. In production, the Public Portal module will be bundled
into the server and serve through Apache Tomcat._


### Run the Admin Portal ###


The "Admin Portal" is an Angular Module and can be found in the directory `tbbtalentv2\ui\admin-portal`.

As for the "Candidate Portal", make sure all libraries are installed locally.

Then from within the same directory run: 

> ng serve

You will see log similar to: 

```
chunk {main} main.js, main.js.map (main) 11.9 kB [initial] [rendered]
chunk {polyfills} polyfills.js, polyfills.js.map (polyfills) 236 kB [initial] [rendered]
chunk {runtime} runtime.js, runtime.js.map (runtime) 6.08 kB [entry] [rendered]
chunk {styles} styles.js, styles.js.map (styles) 16.6 kB [initial] [rendered]
chunk {vendor} vendor.js, vendor.js.map (vendor) 3.55 MB [initial] [rendered]
i ｢wdm｣: Compiled successfully.
```

The Admin Portal is now running locally and you can open a browser (chrome preferred) to: 

[http://localhost:4201](http://localhost:4201)


__Note:__ _this is for development mode only. In production, the Admin Portal module will be bundled 
into the server and serve through Apache Tomcat._ 

### Log In To The Admin Portal ###

- On startup, the server automatically creates a default user with username `SystemAdmin` 
and password `password` that can be used to log in to the admin portal in development.
- Details about this user can be found in `org/tbbtalent/server/configuration/SystemAdminConfiguration.java`

### Populate ElasticSearch from Postgres Database ###

- Log in to Admin Portal as SystemAdmin, go to Settings | Admin API and make API call `esload` 

## Upgrades ##

### Angular ###

See https://update.angular.io

Note that you have to separately upgrade each of the Angular directories:

- ui/admin-portal
- ui/candidate-portal
- ui/public-portal

Assuming that the package.json in each of the above directories has the right
versions already in there you just need run the following commands in each
directory.

> npm install 

Note and fix any errors. "npm outdated" is good for identifying outdated libraries
"npm update --save" will update versions to the latest version within the allowed versions 
specified by the package.json.

Once all versions are updated for the current version of Angular, you can run the Angular
update as follows.
>
> ng update

This will prompt you to update the Angular core and cli. For example: 

> ng update @angular/core@13 @angular/cli@13
 
This will update package.json with the appropriate Angular versions which will drive updates of 
other dependent libraries.

You may find that you need to manually upgrade versions of some tools in package.json so that they 
work with the new version of Angular. 
For example, you might need to upgrade the version of ng-bootstrap to a version that works with the 
later version of Angular. 
Look at the doc of the library in question to select the correct version

You may also need to make changes to your Angular code because of changes in Angular, or because of
changed APIs in the dependent libraries.

### Npm ###

See https://stackoverflow.com/questions/11284634/upgrade-node-js-to-the-latest-version-on-mac-os

## Version Control ##

We use GitHub. Our repository is called tbbtalentv2 - 
[https://github.com/talentbeyondboundaries/tbbtalentv2](https://github.com/talentbeyondboundaries/tbbtalentv2)

See the [GitHub wiki](https://github.com/talentbeyondboundaries/tbbtalentv2/wiki) 
for additional documentation.

### Master branch ###

The main branch is "master". We only merge and push into "master" when we are 
ready to deploy to production (rebuild and upload of build artifacts to the 
production environment is automatic, triggered by any push to "master". 
See Deployment section below).

Master should only be accessed directly when staging
is merged into it, triggering deployment to production. You should not
do normal development in Master.  


### Staging branch ###

The "staging" branch is used for code which is potentially ready to go into
production. Code is pushed into production by merging staging into master and
then pushing master. See Deployment section below. 

Staging is a shared resource so you should only push changes there when
you have finished changes which you are confident will build without error 
and should not break other parts of the code.

As a shared resource, staging is the best way to share your code with other
team members to allow them to merge your code into their own branches and
also to allow them to review your code and help with testing.

Rebuild and upload of build artifacts to the AWS testing environment is automatic when any 
push is made to "staging".

### Personal branches ###

New development should be done in branches. 

Typically, you should branch from the staging branch, and merge regularly 
(eg daily) from staging so that your code does not get too far away from
what everyone else is doing.
  
When you are ready to share your code for others to take a look at and for
final joint testing and eventual deployment, merge your branch into staging.

On your branch you should commit often - doing separate commits for specific
functionality, rather than lumping different kinds of functionality into
a single big commit. That makes commits simpler to review and understand.
It also makes it easier to revert specific functionality when you have got 
something wrong and decide to start again, doing it differently.

You should feel comfortable pushing regularly - often doing Commit and Push 
at the same time. Pushing is effectively saving your work into the "cloud"
rather having changes just saved on your computer.
  
## Deployment and Monitoring ##

See the Deployment and Monitoring pages on the 
[GitHub wiki](https://github.com/talentbeyondboundaries/tbbtalentv2/wiki).

## License
[GNU AGPLv3](https://choosealicense.com/licenses/agpl-3.0/)

