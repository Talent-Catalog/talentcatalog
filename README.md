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

(On a Mac, installing with Homebrew works well. eg "brew install xxx". 
However, Flyway and Postgres don't install with Homebrew, and the book 
"Angular Up & Running" book notes that installing Node.js using Homebrew on a 
can also have problems.)

### Setup your local database ###

Use PostreSQL pgAdmin tool to...

- Create a new login role (ie user) called tbbtalent, password tbbtalent with 
full privileges
- Create a new database called tbbtalent and set tbbtalent as the owner
- The database details are defined in bundle/all/resources/application.yml
- The database is populated/updated using Flyway at start up - see TbbTalentApplication
- Run data migration script to add additional data - using tool like postman or curl 
  - call login http://localhost:8080/api/admin/auth/login
 and save token 
   - call curl -H 'Accept: application/json' -H "Authorization: Bearer ${TOKEN}" http://localhost:8080/api/admin/system/migrate
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

## Version Control ##

We use Bitbucket - [https://bitbucket.org/dashboard/overview]()

Our repository is called tbbtalentv2 - John Cameron is the owner.

### Master branch ###

The main branch is "master". We only merge and push into "master" when we are 
deploying to production (deployment to production is automatic, triggered by any 
push to "master" - see Deployment section below).

Master should only be accessed directly when staging
is merged into it, triggering deployment to production. You should not
do normal development in Master.  


### Staging branch ###

The "staging" branch is used for code which is potentially ready to go into
production. Code it pushed into production by merging staging into master and
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
  
## Deployment ##

### Production ###
Deployment to production is triggered by pushing to the master branch on our
Bitbucket version control. See Version Control section above.

The "master" branch is associated with a pipeline which automatically builds
and deploys (to AWS). This build process is controlled by 
bitbucket-pipelines.yml.

Deployment can take around 10 minutes during which time the production software
is unavailable. People trying to access the software during deployment
will see an error on their browser saying something like "520 Bad Gateway".

### Test ###
We use Heroku to host deployments to a test system.

John Cameron has a Heroku account where there is a server called 
tbbtalent-staging - [https://tbbtalent-staging.herokuapp.com/]()


Once you have installed the Heroku command line 
[https://devcenter.heroku.com/articles/heroku-cli]()
 
... you can add the Heroku remote to your local repository (once only) with 
this command:

> heroku git:remote -a tbbtalent-staging
  
... then you can push your local staging branch any time to Heroku's master branch 
with this command:

> git push heroku staging:master

That will automatically build and deploy to our Heroku test server at
[https://tbbtalent-staging.herokuapp.com/]().

 

