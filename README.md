# TBB Talent Portal #

## Overview ##

This is the repository for the Talent Beyond Boundaries Talent Portal, which manages data 
for refugees looking for skilled migration pathways into safe countries and employment. 
 
This repository is a "mono-repo", meaning it contains multiple sub-modules all of which 
make up the TBB Talent Portal system. In particular it contains: 

- **server**: the backend module of the system providing secure API (REST) access to the 
data, stored in an SQL Database. This module is written in Java / Spring Boot.
- **candidate-portal**: the frontend module through which candidates (refugees seeking skilled 
migration) are able to register and manage their details. This is written in Angular and connects 
to the REST API endpoints under `/api/candidate` provided by the server. 
- **admin-portal**: the frontend module through which TBB staff are able to view, manage and annotate 
candidate details. This is written in Angular and connects to the REST API endpoints under 
`/api/admin` provided by the server.

## Contributing ##

Contributions are very welcome. Please see 
[our contribution guidelines](CONTRIBUTING.md). 
They should be submitted as pull request.
     
## How do I get set up? ##

### Install the tools ###

Download and install the latest of the following tools.

IMPORTANT NOTE FOR MAC Users:

On a Mac, installing with Homebrew usually works well. eg "brew install xxx".
However, Flyway and Postgres don't install with Homebrew, and the book
"Angular Up & Running" notes that installing Node.js using Homebrew
can also have problems. Googling you can still see lots of people having
problems installing Node using brew.

It is also probably easier to install Java directly rather than using brew.


- Java JDK8
   - See [this post](https://stackoverflow.com/questions/24342886/how-to-install-java-8-on-mac)
    

- Gradle [https://gradle.org/install/](https://gradle.org/install/)
  > brew install gradle
- NodeJS: Install as described here [https://nodejs.org/en/](https://nodejs.org/en/)


- Angular CLI [https://angular.io/cli](https://angular.io/cli)
  > npm install -g @angular/cli

- cURL (for database migrations, can also use Postman) 
  > brew install curl

- Docker (we are moving to a container architecture, so want to start
  using Docker technology - in particular for running Elasticsearch - 
  see below)
    - Install Docker Desktop for Mac - 
      see [docker website](https://hub.docker.com/editions/community/docker-ce-desktop-mac/)


- Elasticsearch (for text search)
    - Install Docker image. 
      See [Elastic search website](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html)
      Just pull the image to install. See later for how to run.

- Kibana (for monitoring Elasticsearch)
    - Install Docker image.
      See [Elastic search website](https://www.elastic.co/guide/en/kibana/current/docker.html)
      Just pull the image to install. See later for how to run.

- Git - [see Git website](https://git-scm.com/downloads)
- PostgreSQL - [Postgres website](https://www.postgresql.org/download/)
- IntelliJ IDEA (or the IDE of your choice) - [Intellij website](https://www.jetbrains.com/idea/download/)

### Setup your local database ###

Use PostreSQL pgAdmin tool to...

- Create a new login role (ie user) called tbbtalent, password tbbtalent with 
full privileges
- Create a new database called tbbtalent and set tbbtalent as the owner
- The database details are defined in bundle/all/resources/application.yml
- The database is populated/updated using Flyway at start up - see TbbTalentApplication
- Run data migration script to add additional data - using tool like postman or curl 
    - call login http://localhost:8080/api/admin/auth/login and save token
     
          $ curl -X POST -H ‘Content-Type: application/json’ -d ‘{“username”:”${USERNAME}”,”password”:"${PASSWORD}"}’ http://localhost:8080/api/admin/auth/login

    - call API http://localhost:8080/api/admin/system/migrate with token
       
          $ curl -H 'Accept: application/json' -H "Authorization: Bearer ${TOKEN}" http://localhost:8080/api/admin/system/migrate

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
  environment variables - including programmatic access to TBB's Amazon AWS, 
  Google and Salesforce accounts. If these environment variables are not set
  the application should still run in your development environment, but it may
  not have access to these integrations. Contact TBB if you need access to these
  "secrets". They are stored in a tbb_secrets.txt file which you can hook into
  your start up to set the relevant environment variables.
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

The "Candidate Portal" is an Angular Module and can be found in the diretory `tbbtalentv2\ui\candidate-portal`.

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

## Upgrades ##

### Angular ###

See https://angular-update-guide.firebaseapp.com/

Note that you have to separately upgrade each of the Angular directories:

- ui/admin-portal
- ui/candidate-portal

Assuming that the package.json in each of the above directories has the right
versions already in there you just need run the following commands in each
directory.

> npm install
>
> ng update   

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
and should not not break other parts of the code.

As a shared resource, staging is the best way to share your code with other
team members to allow them to merge your code into their own branches and
also to allow them to review your code and help with testing.

### Personal branches ###

New development should be done in branches. 

Typically you should branch from the staging branch, and merge regularly 
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

