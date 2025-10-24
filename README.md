# Talent Catalog #

## Overview ##

This is the repository for the Talent Catalog (TC), which manages data
for refugees looking for skilled migration pathways into safe countries and employment.

This repository is a "mono-repo", meaning it contains multiple sub-modules all of which
make up the TC system. In particular, it contains:

- **server**: the backend module of the system providing secure API (REST) access to the
  data, stored in an SQL Database. This module is written in Java / Spring Boot.
- **candidate-portal**: the frontend module through which candidates (refugees seeking skilled
  migration) are able to register and manage their details. This is written in Angular and connects
  to the REST API endpoints under `/api/candidate` provided by the server.
- **admin-portal**: the frontend module through which admin staff are able to view, manage and
  annotate
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

> IMPORTANT NOTE:
>
>These instructions are tailored for Mac users using Intellij, as this is what we use for
> development.
>
>On a Mac, installing with Homebrew usually works well. eg "brew install xxx".
>
>It is also probably easier to install Java directly (or from your
> development IDE - see below) rather than using brew.

Download and install the latest of the following tools.

- Homebrew - [Homebrew website](https://brew.sh)

- IntelliJ IDEA - [Intellij website](https://www.jetbrains.com/idea/download/)
    - Import standard settings and run configurations from another developer
    - In development, it is best to build using Intellij rather than gradle. Change the Intellij
      setting for "Build, Execution & Deployment" > "Build Tools" > "Gradle" to build with Intellij.

- Java 17
    - The current version of Java supported is Java 17. We use the Temurin release (however there
      should be no issues using other releases). **One way** (but you can choose whichever method
      you like) to manage Java versions is with **sdkman**. A .sdkmanrc file
      exists when you check out the repository. You can get **sdkman** by running the following:

      ```
      curl -s "https://get.sdkman.io" | bash
      source "$HOME/.sdkman/bin/sdkman-init.sh"
      sdk install 17.0.11-tem
      ```

    - Intellij will load the JDK through the .sdkmanrc file.
    - Update the Project SDK:
        - Go to File / Project Structure / Project and set the SDK to your chosen JDK.
        - On the same page, ensure the language level matches your chosen SDK version.
    - IntelliJ Settings:
        - Go to IntelliJ / Settings / Build,Execution,Deployment / Compiler / Java Compiler
            - Add `-parameters` to the`Additional command line parameters` textbox.
            - Set the `Project bytecode version` to match the JDK chosen (e.g. **17**).
        - Go to IntelliJ / Settings / Build,Execution,Deployment / Build Tools / Gradle
            - Set the **GradleJVM** from the drop list to use the Project SDK.
- Code Style
    - Download the intellij-java-google-style.xml file from the google/styleguide repository
      [here](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml).
    - Launch IntelliJ and go to the **IntelliJ > Settings...** menu and expand the **Code Style**
      sub-menu underneath Editor. Here, you will see a list of supported languages. Select **Java**.
    - Next to the Scheme drop-down menu select the gear icon then **Import Scheme > IntelliJ IDEA
      code
      style XML** then select the intellij-java-google-style.xml file you downloaded from GitHub.
    - Give the schema a name (or use the default GoogeStyle name from the import). Click **OK** or
      **Apply** for the settings to take effect.


- Python 3.12
    - Later versions of Python are not supported by all the packages that we use.
    - It is not recommended to install Python using brew.
      See, for example, [here](https://pydevtools.com/handbook/explanation/should-i-use-homebrew-to-install-python/).
      Instead, download from the [Python website](https://www.python.org/downloads/).
    - Intellij Settings:
        - See [Intellij doc](https://www.jetbrains.com/help/idea/configuring-python-sdk.html).
          We configure local Python interpreters using [virtual environments](https://www.w3schools.com/python/python_virtualenv.asp).

- Gradle [https://gradle.org/install/](https://gradle.org/install/)
  ```
  brew install gradle
  ```

- Node [https://nodejs.org/en/](https://nodejs.org/en/)
    - Note that developers should use **Node version 18**, specifically versions **18.10.0 and 
    above**, which is currently the latest LTS (Long Term Support) version compatible with Angular 16.
        - See [Angular Compatibility Table](https://angular.io/guide/versions)
        - See [Node.js Releases](https://nodejs.org/en/about/releases/)
    - If using Node 17 or higher, it’s recommended to add `--host=127.0.0.1` to the `ng serve` 
    command to avoid debugger and sourcemap issues in IntelliJ. The `start` scripts in `package.json`
    have been bundled with this parameter for convenience.
        - See [IntelliJ Angular Debugging Guide](https://www.jetbrains.com/help/idea/angular.html)
        - See [IntelliJ Angular Debugging Troubleshooting](https://www.jetbrains.com/help/idea/angular.html#ws_angular_debug_app_troubleshooting)

  ```
  brew install node@18
  ```
    - Note the messages from brew at the end of the install.
      You will have to manually set up the path.


- Angular CLI [https://angular.io/cli](https://angular.io/cli)
  ```
  npm install -g @angular/cli@16
  ```
    - See https://angular.io/guide/versions
    - To upgrade Angular versions, see https://update.angular.io/


- Git - [see Git website](https://git-scm.com/downloads) - Not really necessary now with Intellij
  which will prompt you to install Git if needed.


- Docker and docker-compose
    - Install Docker Desktop for Mac -
      see [docker website](https://hub.docker.com/editions/community/docker-ce-desktop-mac/)
    - Note for Mac Silicon users. The current Docker doc (link above) implies that installing
      Rosetta is optional.
      But if you don't do it you won't be able to install Docker.
      You need to execute softwareupdate --install-rosetta just to run Docker for the first time
      after installing it.
    - When you install Docker Desktop for Mac, Docker Compose is bundled with it. You can verify the 
      installation by running:
      ```shell
        docker-compose --version
      ```

### Clone the TC repository from Git ###

- Clone [the repository](https://github.com/Talent-Catalog/talentcatalog.git) to your local system
```shell
git clone https://github.com/Talent-Catalog/talentcatalog.git
```
- Open the root folder in IntelliJ IDEA (it should auto detect gradle and self-configure)

### Using Docker-Compose to Start Services ###

With Docker and Docker Compose installed, you can now use docker-compose to set up the required 
services: PostgreSQL, Redis, Elasticsearch, and optionally, Kibana.

- The TC repository includes a docker-compose.yml file in the docker-compose folder, 
with preconfigured services for PostgreSQL, Redis, Elasticsearch, and Kibana. This file is ready 
for you to use.
- To start the services, navigate to the docker-compose folder and run the following command:
```shell
cd talentcatalog/docker-compose
docker-compose up -d
```
- The -d flag runs the services in detached mode.
- To stop the services, run the following command:
```shell
docker-compose down
```

### Using IntelliJ’s Docker-Compose Integration to Start Services ###

IntelliJ IDEA provides built-in support for Docker Compose, allowing you to start and stop services 
directly from the IDE, either from the Services tool window or directly from the docker-compose.yml 
file itself.

- In the Project tool window, navigate to and open the docker-compose.yml file.
- IntelliJ adds green Run/Debug triangles in the gutter (left margin) next to each service in the 
docker-compose.yml file.
- Click on the green Run triangle next to a service (e.g., postgres or redis) to start that specific 
service.
- You can also click the Run triangle next to the services block at the top of the file to start all 
services at once.

### Verify Services ###

The following services will all run from the Docker container:

- **PostgreSQL** (listening on port 5432)
- **Redis** (6379)
- **Elasticsearch** (9200)
- **Kibana** (5601)

Verify with the following terminal command: 
```shell
docker ps
```

### AWS management tools ###

These tools do not need to be installed in order to get the code up and running on your development
machine. However, they are needed if you want to build the TC's AWS cloud infrastructure
from the Terraform definitions in the `infra` folder.

- AWS
  CLI - [see AWS CLI website](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)

Once installed, needs to be configured. Log in to your AWS account, click on user top right,
select Security Credentials, create access key, then download to CSV file.
Then, theoretically this should work

   ```
   aws configure import --csv path-to-downloaded-file.csv
   ```

...but it doesn't currently (it fails saying that it is missing a User Name header). Instead,
just run this and manually copy/paste the values from the CSV file as prompted.

   ```
   aws configure
   ```

- Terraform (for setting up our AWS infrastructure). 
- NOTE: Due to Terraform licensing changes as simple "brew install terraform" no longer works.
   ```
   brew tap hashicorp/tap
   brew install hashicorp/tap/terraform
   ```

Once installed, you can run Terraform from the directory containing your main Terraform
file (main.tf).
In order to populate "secret" configuration values that Terraform needs to set up as
environment variables for the TC software, you need to copy a special file `terraform.tfvars`
to that directory before running terraform. Contact support@talentcatalog.net for a copy of that
file.

Then you can run `init` (only need to do this once), and then `plan` or `apply`, as needed.

   ```
   terraform init
   ```

   ```
   terraform plan
   ```

   ``` 
   terraform apply
   ```

### Set up your local database ###

Ask TC developers for a `pg_dump` of the database. Note that the dump does not have to be recent. 
The software will automatically apply any required updates to the database definition, driven by 
Flyway files stored in GitHub. 

A standard dump file is kept specifically for getting new developers started, but TC developers can 
also quickly create a new one from their local containerised version with the following commands:

   ```shell    
   docker exec -it docker-compose-postgres-1 pg_dump --file=/tmp/tcdump.sql --create --username=tctalent --host=localhost --port=5432
   ```
   ```shell    
   docker cp docker-compose-postgres-1:/tmp/tcdump.sql </path/to/file>   
   ```

Once you have the dump, run Docker-Compose and check that your newly created `tctalent` database and 
user are up and running by accessing the psql console:
```shell
docker exec -it docker-compose-postgres-1 psql -U tctalent -d tctalent
```
It should open with `tctalent=#` as prompt. If you get an error, return to the Docker-Compose setup 
process.

Otherwise, `\q` will exit the console. You can then copy the dump file to your Docker container and 
use it to populate your empty database:

   ```shell
   docker cp <path/to/file> docker-compose-postgres-1:/tmp/dump.sql
   ```
   ```shell
   docker exec -it docker-compose-postgres-1 psql -U tctalent -d tctalent -f /tmp/dump.sql
   ```

### Connect IntelliJ to your database ###
- File > New > Data Source > PostgreSQL > PostgreSQL
- Give the DB a name that clearly identifies it as your local development version.
- Populate the other setup parameters with the default values in the `postgres` configuration of the project file `docker-compose.yml`.

### Run the server ###

- Some secret information such as passwords and private keys are set in
  environment variables - including programmatic access to TC's Amazon AWS,
  Google and Salesforce accounts. If these environment variables are not set
  the application will fail at start up. Contact other TC developers for a copy of
  a "secrets" file suitable for developers.
  On development computers you hook the file, tc_secrets.txt, into your computer's start up to
  set the relevant environment variables.
  For example add "source ~/tc_secrets.txt" to .bash_profile or .zshenv
  depending on whether you are running bash or zsh.

- Create a new Run Profile for `org.tctalent.server.TcTalentApplication`.
  In the Environment Variables section of Intellij, check the
  "Include system environment variables" checkbox.
- Run the new profile, you should see something similar to this in the logs:

```
Started TcTalentApplication in 2.217 seconds (JVM running for 2.99)
```

- your server will be running on port 8080 (default for Spring Boot)
  (can be overridden by setting server.port in application.yml, or Intellij Run
  Configuration, and updating environment.ts in portals)
- To test it open a browser to [http://localhost:8080/test](http://localhost:8080/test)

### Run the Candidate Portal ###

The "Candidate Portal" is an Angular Module and can be found in the directory
`talentcatalog\ui\candidate-portal`.

Before running, make sure all the libraries have been downloaded locally by running `npm install`
from the root directory of the module (i.e. `talentcatalog\ui\candidate-portal`):

   ```
   cd talentcatalog\ui\candidate-portal
   npm install
   ```

It is also a good idea to install fsevents for MacOS which will greatly
reduce your CPU usage

   ```
   npm install fsevents
   npm rebuild fsevents
   ``` 

Then from within the same directory run:

   ```
   npm start
   ```

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

__Note:__ _this is for development mode only. In production, the Candidate Portal module will be
bundled
into the server and serve through Apache Tomcat._

### Run the Public Portal ###

The "Public Portal" is an Angular Module and can be found in the
directory `talentcatalog\ui\public-portal`.

As for the "Candidate Portal", make sure all libraries are installed locally.

Then from within the same directory run:

   ```
   npm start
   ```

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

__Note:__ _this is for development mode only. In production, the Public Portal module will be
bundled
into the server and serve through Apache Tomcat._

### Run the Admin Portal ###

The "Admin Portal" is an Angular Module and can be found in the directory
`talentcatalog\ui\admin-portal`.

As for the "Candidate Portal", make sure all libraries are installed locally.

Then from within the same directory run:

   ```
   ng serve
   ```

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
- Details about this user can be found in
  `org/talentcatalog/server/configuration/SystemAdminConfiguration.java`

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

   ```
   npm install 
   ```

Note and fix any errors. "npm outdated" is good for identifying outdated libraries
"npm update --save" will update versions to the latest version within the allowed versions
specified by the package.json.

Once all versions are updated for the current version of Angular, you can run the Angular
update as follows.

   ```
   ng update
   ```

This will prompt you to update the Angular core and cli. For example:

   ```
   ng update @angular/core@13 @angular/cli@13
   ```

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

We use GitHub. Our repository is called talentcatalog -
[https://github.com/Talent-Catalog/talentcatalog](https://github.com/Talent-Catalog/talentcatalog)

See the [GitHub wiki](https://github.com/Talent-Catalog/talentcatalog/wiki)
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
[GitHub wiki](https://github.com/Talent-Catalog/talentcatalog/wiki).

## License

[GNU AGPLv3](https://choosealicense.com/licenses/agpl-3.0/)

