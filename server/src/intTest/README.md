# Integration Tests

## Purpose
The `src/intTest` directory contains files, source code and scripts that provide a capability 
for executing integration tests. 

### Contents
`intTest/db` contains resources required for set up and operation of a transient database. It 
has a `docker-compose` file that can be used to create the relevant postgres docker container, 
to be used alongside the `postgres.env` file. *Most importantly* the integration-test-dump.sql 
file contains the dump file that will be used in the integration testing. This file has a small 
set of populated tables but is primarily empty and should be run in order to support the flyway 
scripts that will execute on application startup.

`intTest/kotlin` contains the test suites and other source code.

`intTest/resources` contains logging, application and testcontainer configuration files.

### Setup summary
The integration tests make use of [Testcontainers](https://testcontainers.com/). There are 
several requirements for this to operate correctly.
* An environment that will support containers, such as Docker or Colima. 
  * Note: If using Colima, you must start with the appropriate flags: 
    * `colima start --network-address ...`
    * Set the following in your bash or zshrc file.
    ```
    export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
    export TESTCONTAINERS_HOST_OVERRIDE=$(colima ls -j | jq -r '.address')
    export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"
    ```
    * If you do not have jq installed, install that and re-source your file. 
    ```
    brew install jq
    source ~/.zshrc
    * ```

* You have started the docker container, e.g.
  * `docker-compose --file server/src/intTest/db/docker-compose.yml up -d`
* You have the dump file in the correct location specified above `src/db`. 

Kotlin has been chosen to write the tests given its ease of use, expressiveness and native 
integration with Java.  

### Running the tests
From the command line, you can simply run the existing build file:
* `./gradlew clean intTest` or `./gradlew clean server:check`
* Of course, you can run the tests from IntelliJ as you normally would.

### Repository Integration Tests
Tests have been written to provide coverage on the classes used to access data, within the `org.
tctalent.server.repository.db` package. All test classes that wish to use the Testcontainers 
infrastructure should extend the `BaseDBIntegrationTest`. This class will handle all setup and 
launching/shutdown of the container. 

There is a general and obvious pattern used in the test classes. Autowiring repositories that 
are used to interact with the database (saving/retrieving data). 

A ` fun setup() ` function is used that will be called with `@BeforeTest` annotation to be 
executed before all tests. Again, there is no need to manage resources.

Also of note is a file (`org.tctalent.server.repository.db.integrationhelp.DomainHelpers.kt`) 
containing helper 
functions to create data as 
required. 

