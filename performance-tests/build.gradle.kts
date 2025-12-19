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

val scalaVersion: String = "3.4.1"
val typesafeConfigVersion: String = "1.4.3"
val postgresVersion: String = "42.6.0"
val gatlingVersion: String = "0.10.3"
val highchartsVersion: String = "3.9.5"
val javaTargetVersion = JavaVersion.VERSION_17
val scalaCompileOpts: String = "-release:17"
val testName: String = "gatlingTest"

plugins {
    id("io.gatling.gradle") version "3.10.5.1"
    scala
}

scala {
    version = scalaVersion
}

group = "org.talentcatalog"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala3-library_3:3.4.1")
    testImplementation("io.gatling.highcharts:gatling-charts-highcharts:$highchartsVersion")
    testImplementation("ru.tinkoff:gatling-jdbc-plugin_2.13:$gatlingVersion")
    testImplementation("org.postgresql:postgresql:$postgresVersion")
    testImplementation("com.typesafe:config:$typesafeConfigVersion")
}

tasks {
    create<JavaExec>("gatlingTest") {
        description = "Run Gatling tests"
        classpath = sourceSets["test"].runtimeClasspath
        mainClass = "io.gatling.app.Gatling"
        args = listOf(
            "-s", "org.talentcatalog.PostgresLoadTest", "-rf", "build/reports/gatling"
        )
    }

    withType<ScalaCompile> {
        scalaCompileOptions.additionalParameters = listOf(scalaCompileOpts)
        scalaCompileOptions.forkOptions.apply {
            memoryMaximumSize = "1g"
            jvmArgs = listOf("-XX:MaxMetaspaceSize=512m")
        }
    }

    withType<Test> {
        dependsOn(testName)
    }
}
