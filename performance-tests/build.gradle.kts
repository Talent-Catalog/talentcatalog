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

val scalaVersion = "2.13.12"
val typesafeConfigVersion = "1.4.3"
val postgresVersion = "42.6.0"

// JDBC plugin version variable; it's NOT the Gatling version.
val gatlingJdbcPluginVersion = "0.10.3"

// Gatling versions should match plugin / core dependencies.
val gatlingVersion = "3.10.5"
val highchartsVersion = "3.9.5"

val javaTargetVersion = JavaVersion.VERSION_17
val scalaCompileOpts = listOf("-release:17")

plugins {
    id("io.gatling.gradle") version "3.10.5.1"
    scala
    java
}

group = "org.talentcatalog"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    // Compile our Java source code as Java 17 source.
    sourceCompatibility = javaTargetVersion
    targetCompatibility = javaTargetVersion
}

scala {
    // It speeds up Scala compilation by recompiling only what changed
    zincVersion = "1.9.6"
}

dependencies {
    // Scala runtime for existing Scala simulations
    gatlingImplementation("org.scala-lang:scala-library:$scalaVersion")

    // Gatling core + charts (Scala)
    gatlingImplementation("io.gatling:gatling-core:$gatlingVersion")
    gatlingImplementation("io.gatling.highcharts:gatling-charts-highcharts:$highchartsVersion")

    // Java DSL modules (correct ones)
    gatlingImplementation("io.gatling:gatling-core-java:$gatlingVersion")
    gatlingImplementation("io.gatling:gatling-http-java:$gatlingVersion")

    // JDBC plugin (Scala 2.13 binary)
    gatlingImplementation("ru.tinkoff:gatling-jdbc-plugin_2.13:$gatlingJdbcPluginVersion")

    // DB + config dependencies used by existing simulations
    gatlingImplementation("org.postgresql:postgresql:$postgresVersion")
    gatlingImplementation("com.typesafe:config:$typesafeConfigVersion")
}


gatling {
    // Ensures Gatling uses src/gatling/** conventions
    includeMainOutput = false
    includeTestOutput = false
}

tasks {

    // Standard: compile Scala with JDK 17 compatibility
    withType<ScalaCompile> {
        scalaCompileOptions.additionalParameters = scalaCompileOpts
        scalaCompileOptions.forkOptions.apply {
            memoryMaximumSize = "1g"
            jvmArgs = listOf("-XX:MaxMetaspaceSize=512m")
        }
    }

    /**
     * Custom task: run Gatling simulation by class name
     * Usage:
     *   ./gradlew :performance-tests:gatlingTest -PsimClass=org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchSequentialABSimulation
     *
     */
    register<JavaExec>("gatlingTest") {
        description = "Run Gatling performance tests (Scala or Java)"
        group = "verification"

        val simClass = (project.findProperty("simClass") as String?)
            ?: "org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchSequentialABSimulation"


        classpath = sourceSets["gatling"].runtimeClasspath
        mainClass.set("io.gatling.app.Gatling")

        args = listOf(
            "-s", simClass,
            "-rf", "build/reports/gatling"
        )
    }
}
