# TBB Talent Portal #

## Overview ##

This is the repository for the Talent Beyond Boundaries Talent Portal, which manages data 
for refugees looking for skilled migration pathways into safe countries and employment. 
 
This repository is a "mono-repo", meaning it contains multiple sub-modules all of which 
make up the TBB Talent Portal system. In particular it contains: 

- **server**: the backend module of the system providing secure API (REST) access to the 
data, stored in an SQL Database. This module is written in Java / Spring Boot.
- **candidate-portal (coming soon)**: the frontend module through which candidates (refugees seeking skilled 
migration) are able to register and manage their details. This is written in Angular and connects 
to the REST API endpoints under `/api/candidate` provided by the server. 
- **admin-portal (coming soon)**: the frontend module through which TBB staff are able to view, manage and annotate 
candidate details. This is written in Angular and connects to the REST API endpoints under 
`/api/admin` provided by the server.
     
## How do I get set up? ##

### Install the tools ###

Download and install the latest of the following tools: 

- Git [https://git-scm.com/downloads]()
- Java JDK8 [https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html]()
- Gradle [https://gradle.org/install/]()
- PostgreSQL [https://www.postgresql.org/download/]()
- NodeJS [https://nodejs.org/en/]()
- Angular CLI [https://angular.io/cli]()
- Flyway [https://flywaydb.org/]()
- IntelliJ IDEA (or the IDE of your choice) [https://www.jetbrains.com/idea/download/]()

(On a Mac, installing with Homebrew works well. eg "brew install node" to install Node.js. 
However, Flyway and Postgres don't install with Homebrew)

### Setup your local database ###

Use PostreSQL pgAdmin tool to...

- Create a new login role (ie user) called tbbtalent, password tbbtalent with 
full privileges
- Create a new database called tbbtalent and set tbbtalent as the owner
- The database details are defined in bundle/all/resources/application.yml
- The database is populated/updated using Flyway at start up - see TbbTalentApplication
- Run data migration script to add additional data - using tool like postman or curl call login http://localhost:8080/api/admin/auth/login
 and save token call curl -H 'Accept: application/json' -H "Authorization: Bearer ${TOKEN}" http://localhost:8080/api/admin/system/migrate
### Download and edit the code ###

- Clone [the repository](https://bitbucket.org/johncameron/tbbtalentv2/src/master/) to your local system
- Open the root folder in IntelliJ IDEA (it should auto detect gradle and self-configure)

### Run the server ###

- Create a new Run Profile for `org.tbbtalent.server.TbbTalentApplication`
- Run the new profile, you should see something similar to this in the logs: 
```
Started TbbTalentApplication in 2.217 seconds (JVM running for 2.99)
```
- your server will be running on port 8080 
(can be overriden by setting server.port and updating environment.ts in portals)
- To test it open a browser to [http://localhost:8080/test]()


### Run the Candidate Portal ###

The "Candidate Portal" is an Angular Module and can be found in the diretory `tbbtalentv2\ui\candidate-portal`.

Before running, make sure all the libraries have been downloaded locally by running `npm install` from the root 
directory of the module (i.e. `tbbtalentv2\ui\candidate-portal`):

> cd tbbtalentv2\ui\candidate-portal
> npm install 

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

The Candidate Portal is now running locally and you can open a browser (chrome preferred)to: 

[http://localhost:4200]()


__Note:__ _this is for development mode only. In production, the Candidate Portal module will be bundled 
into the server and serve through Apache Tomcat._  


### Run the Admin Portal ###


The "Admin Portal" is an Angular Module and can be found in the directory `tbbtalentv2\ui\admin-portal`.

Before running, make sure all the libraries have been downloaded locally by running `npm install` from the root 
directory of the module (i.e. `tbbtalentv2\ui\admin-portal`):

> cd tbbtalentv2\ui\admin-portal
> npm install 

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

The Admin Portal is now running locally and you can open a browser (chrome preferred)to: 

[http://localhost:4201]()


__Note:__ _this is for development mode only. In production, the Admin Portal module will be bundled 
into the server and serve through Apache Tomcat._ 
