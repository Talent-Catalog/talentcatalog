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

IMPORTANT NOTE:

Below instructions are tailored to Mac users, as this is what we use for development. Adittional notes have been added 
for certain steps required for newer macs with the M1 chip. Windows/Linux users can also run the program, but will need 
to adjust the set up accordingly based on their system requirements. 

On a Mac, installing with Homebrew usually works well. eg "brew install xxx". However, the book "Angular Up & Running" 
notes that installing Node.js using Homebrew can have problems. Googling you can still see lots of people having
issues installing Node using brew.

It is also probably easier to install Java directly (or from your development IDE - see below) rather than using brew.

- IntelliJ IDEA (or the IDE of your choice) - [Intellij website](https://www.jetbrains.com/idea/download/)


- Java 11
   - At least Java 11 is required because we use the Locale object to provide translations of countries and languages and 
  that support is not complete in Java 8, for example.
   - If you are using a recent version of Intellij the version of Java that comes with it works fine except that it does 
  not have library source code. You will need to download a new SDK (which you can from inside Intellij).
   - [Alternatively install with Homebrew](https://formulae.brew.sh/formula/openjdk) (untested with M1)


- Gradle [https://gradle.org/install/](https://gradle.org/install/)
  > brew install gradle
  
- NodeJS: Install as described [here](https://nodejs.org/en/)
    - Note that you should use the LTS version of node - which is not normally the latest.
    "Production applications should only use Active LTS or Maintenance LTS releases." - https://nodejs.org/en/about/releases/


- Angular CLI [https://angular.io/cli](https://angular.io/cli)
  > npm install -g @angular/cli
  - To upgrade Angular versions, see https://update.angular.io/
  - M1 Tip: If you see an EACCES error when installing Angular, follow instructions listed [here.](https://docs.npmjs.com/resolving-eacces-permissions-errors-when-installing-packages-globally)
    

- cURL (for database migrations, can also use Postman) 
  > brew install curl
  > 
  > or...
  > 
  > brew install --cask postman
  
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


- PostgreSQL 
  > brew install postgres
  - Alternatively: [Postgres website](https://www.postgresql.org/download/)
  - M1 Tip: If you see an issue with the SCHMALL value after installing postgres
    - This works as a temporary fix until reboot:
      > sudo sysctl -w kern.sysv.shmmax=12582912  
    sudo sysctl -w kern.sysv.shmall=12582912
    - Not easy to fix permanently, but instructions on how can be found [here.](https://dansketcher.com/2021/03/30/shmmax-error-on-big-sur/)

    
- pgAdmin (interactive GUI for PostgreSQL)
  - BEFORE installing pgAdmin, you will need to create a default postgres user to log into pgAdmin with.
  - Instructions to do so can be found [here](https://dev.to/letsbsocial1/installing-pgadmin-only-after-installing-postgresql-with-homebrew-part-2-4k44)
    (scroll to section after "Installing PostgreSQL with Homebrew").
  - Then continue with pgAdmin install:
    > brew install --cask pgadmin4
    - Alternatively: [PgAdmin website](https://www.pgadmin.org)

- MySQL - We use MySQL to do daily uploads to the RefugeeTalent database. We are locked into an old
  (pre the Oracle purchase of MySQL) version, 5.7, of MySQL. The best way to install this on a Mac is using brew. 
  - See [Installing MySQL 5.7 using Homebrew](https://medium.com/macoclock/installing-mysql-5-7-using-homebrew-974cc2d42509).
  - Note the brew instructions at the end of the install, particularly the export to the path and the brew services restart.
  - M1 Tip: The export path noted in the above article may not be correct for M1 machines. 
    - If you get an error later on stating that mysql commands are not found by the terminal, this is likely because the 
    export path was not correct.
    - The below command may work for your M1 machine, or you will need to identify the correct path to your mysql install
    > echo 'export PATH="/opt/homebrew/opt/mysql@5.7/bin:$PATH"' >> ~/.zshrc 
    
- Flyway (used to manage database migrations)
  > brew install flyway

### Setup your local database ###

Use PostreSQL pgAdmin tool to...

- Create a new login role (ie user) called tbbtalent, password tbbtalent with full privileges
- Create a new database called tbbtalent and set tbbtalent as the owner
  - Step-by-step instructions on how to do this found [here.](https://www.guru99.com/postgresql-create-alter-add-user.html)
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
  For example add "source ~/tbb_secrets.txt" to .bash_profile or .zshenv
  depending on whether you are running bash or zsh.

- If it doesn't already exist, create a new Run Profile for `org.tbbtalent.server.TbbTalentApplication`. 
  In the Environment Variables section of Intellij, check the "Include system environment variables" checkbox.

- Run the new profile, you should see something similar to this in the logs: 
```
Started TbbTalentApplication in 2.217 seconds (JVM running for 2.99)
```
- The server will be running on port 8080 (default for Spring Boot) 
(can be overridden by setting server.port in application.yml, or Intellij Run 
  Configuration, and updating environment.ts in portals)
- To test it open a browser to [http://localhost:8080/test](http://localhost:8080/test)

- M1 Tip: If you see a Netty Warning on startup, this is normal and you can ignore it. See discussion of this [here.](https://rieckpil.de/java-development-on-an-apple-m1-a-one-year-review/)

### Run the Candidate Portal ###

The "Candidate Portal" is an Angular Module and can be found in the diretory `tbbtalentv2\ui\candidate-portal`.

Tip: If you see this error: `ng: command not found` when running any of the below commands, first confirm that node is 
correctly installed. If node is correctly installed, you may need to run `npm link @angular/cli` as described [here.](https://stackoverflow.com/questions/46623571/angular-ng-command-not-found)

- First, create a Run Profile in IntelliJ that opens the Candidate Portal on [http://localhost:4200](http://localhost:4200).

- Before running, make sure all the libraries have been downloaded locally by running `npm install` from the root 
directory of the module (i.e. `tbbtalentv2\ui\candidate-portal`):

    > cd tbbtalentv2\ui\candidate-portal
    >
    > npm install

- It is also a good idea to install fsevents for MacOS which will greatly
reduce your CPU usage

    > npm install fsevents
    > 
    > npm rebuild fsevents

- Then from within the same directory run: 

    > ng serve

- You will see log similar to: 

    ```
    chunk {main} main.js, main.js.map (main) 11.9 kB [initial] [rendered]
    chunk {polyfills} polyfills.js, polyfills.js.map (polyfills) 236 kB [initial] [rendered]
    chunk {runtime} runtime.js, runtime.js.map (runtime) 6.08 kB [entry] [rendered]
    chunk {styles} styles.js, styles.js.map (styles) 16.6 kB [initial] [rendered]
    chunk {vendor} vendor.js, vendor.js.map (vendor) 3.55 MB [initial] [rendered]
    i ｢wdm｣: Compiled successfully.
    ```

- The Candidate Portal is now running locally and you can open a browser (chrome preferred) to: 

    [http://localhost:4200](http://localhost:4200)


__Note:__ _this is for development mode only. In production, the Candidate Portal module will be bundled 
into the server and serve through Apache Tomcat._  


### Run the Admin Portal ###


The "Admin Portal" is an Angular Module and can be found in the directory `tbbtalentv2\ui\admin-portal`.

- First, create a Run Profile in IntelliJ which opens the Admin Portal on [http://localhost:4201](http://localhost:4201)
- As for the "Candidate Portal", make sure all libraries are installed locally.

- Then from within the same directory run: 

    > ng serve

- You will see log similar to: 

    ```
    chunk {main} main.js, main.js.map (main) 11.9 kB [initial] [rendered]
    chunk {polyfills} polyfills.js, polyfills.js.map (polyfills) 236 kB [initial] [rendered]
    chunk {runtime} runtime.js, runtime.js.map (runtime) 6.08 kB [entry] [rendered]
    chunk {styles} styles.js, styles.js.map (styles) 16.6 kB [initial] [rendered]
    chunk {vendor} vendor.js, vendor.js.map (vendor) 3.55 MB [initial] [rendered]
    i ｢wdm｣: Compiled successfully.
    ```

- The Admin Portal is now running locally and you can open a browser (chrome preferred) to: 

    [http://localhost:4201](http://localhost:4201)


__Note:__ _this is for development mode only. In production, the Admin Portal module will be bundled 
into the server and serve through Apache Tomcat._ 

### Log In To The Admin Portal ###

- On startup, the server automatically creates a default user with username `SystemAdmin` and password `password` 
  that can be used to log in to the admin portal in development. 
- Details about this user can be found in `org/tbbtalent/server/configuration/SystemAdminConfiguration.java`

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

