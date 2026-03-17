/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

val typesafeConfigVersion = "1.4.3"
val postgresVersion = "42.6.0"

// Gatling versions should match plugin / core dependencies.
val gatlingVersion = "3.10.5"

val javaTargetVersion = JavaVersion.VERSION_17

plugins {
    id("io.gatling.gradle") version "3.10.5.1"
    java
}

group = "org.talentcatalog"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = javaTargetVersion
    targetCompatibility = javaTargetVersion
}

dependencies {
    // Java DSL modules
    gatlingImplementation("io.gatling:gatling-core-java:$gatlingVersion")
    gatlingImplementation("io.gatling:gatling-http-java:$gatlingVersion")

    // DB + config dependencies used by simulations
    gatlingImplementation("org.postgresql:postgresql:$postgresVersion")
    gatlingImplementation("com.typesafe:config:$typesafeConfigVersion")
    gatlingImplementation("com.zaxxer:HikariCP:5.1.0")
}

gatling {
    includeMainOutput = false
    includeTestOutput = false
}

sourceSets {
    named("gatling") {
        java.setSrcDirs(listOf("src/gatling/java"))
        resources.setSrcDirs(listOf("src/gatling/resources"))
    }
}

tasks.register<JavaExec>("gatlingTest") {
    description = "Run Gatling performance tests (Java)"
    group = "verification"

    val simClass = (project.findProperty("simClass") as String?)
        ?: "org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchSequentialABSimulation"

    classpath = sourceSets["gatling"].runtimeClasspath
    mainClass.set("io.gatling.app.Gatling")

    // Forward -D properties (like -DlistId=1283) into the Gatling JVM
    systemProperties(
        System.getProperties().entries.associate { (k, v) ->
            k.toString() to v
        }
    )
    // Report folder root for Gatling HTML output.
    // Default matches the old behavior.
    // CI can override using:
    //   -Dgatling.reportRoot=/tmp/tc-gatling-reports
    val reportRoot = System.getProperty("gatling.reportRoot", "build/reports/gatling")

    args(
        "-s", simClass,
        "-rf", reportRoot
    )
}

// Safety net (if a Scala task exists from old config or caches)
tasks.matching { it.name == "compileGatlingScala" }.configureEach {
    enabled = false
}
