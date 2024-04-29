/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

val kotlinVersion: String by project
val lombokVersion: String by project
val apachePoiVersion: String = "5.2.5"
val jwtVersion: String = "0.11.5"
val shedlockVersion: String = "5.10.0"
val javaTargetVersion = JavaVersion.VERSION_21
val kotlinTargetVersion = JavaVersion.VERSION_21

val isDev: Boolean = System.getenv("dev")?.let { it.toBoolean() } ?: false

plugins {
    application
    idea
    java
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.23"
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.0"
    id("com.google.cloud.tools.jib") version "3.3.1"
    id("io.freefair.lombok") version "8.6"
}

group = "org.tctalent"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = javaTargetVersion
    targetCompatibility = javaTargetVersion
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(kotlinTargetVersion.majorVersion))
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    /* This is required as many transient deps trying to call in at runtime */
    configurations.all {
        exclude("commons-logging", "commons-logging")
    }
    /* Some kotlin functional nice libs */
    implementation(platform("io.arrow-kt:arrow-stack:1.2.0"))
    implementation("io.arrow-kt:arrow-core")

    /* Lombok */
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")

//    if (!isDev) {
//        implementation(project(":ui:admin-portal"))
//        implementation(project(":ui:candidate-portal"))
//        implementation(project(":ui:public-portal"))
//    }

    /* Google dependencies */
    implementation("com.google.guava:guava:32.1.1-jre")
    implementation (platform("com.google.cloud:libraries-bom:26.20.0"))
    implementation("com.google.cloud:google-cloud-storage")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0")
    implementation("com.google.api-client:google-api-client:1.32.1")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.32.1")
    implementation("com.google.apis:google-api-services-drive:v3-rev20210725-1.32.1")

    /* Spring deps */
    compileOnly("org.springframework.boot:spring-boot-devtools")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.security:spring-security-web")
    implementation("org.springframework.security:spring-security-messaging")
    implementation("org.springframework.data:spring-data-elasticsearch")
//    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    /* Apache */
    implementation("org.apache.poi:poi-ooxml:$apachePoiVersion")
    implementation("org.apache.poi:poi-scratchpad:$apachePoiVersion")
    implementation("org.apache.pdfbox:pdfbox:2.0.31")
    implementation("org.bouncycastle:bcprov-jdk16:1.45")

    /* Json Web Tokens */
    implementation("io.jsonwebtoken:jjwt-api:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jwtVersion")

    /* Other random */
    implementation("dev.samstevens.totp:totp-spring-boot-starter:1.7.1")
    implementation("com.opencsv:opencsv:5.9")
    implementation("net.javacrumbs.shedlock:shedlock-spring:$shedlockVersion")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion")

    implementation("commons-beanutils:commons-beanutils:1.9.4")
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.638")
    implementation("org.xhtmlrenderer:flying-saucer-pdf-itext5:9.1.22")
    implementation("com.slack.api:slack-api-client:1.39.0")
    implementation("net.sf.jtidy:jtidy:r938")
    implementation("org.jsoup:jsoup:1.15.4")
    implementation("org.jdom:jdom2")
    implementation("org.thymeleaf.extras:thymeleaf-extras-java8time:3.0.4.RELEASE")

    implementation("org.flywaydb:flyway-core")
    implementation("com.sun.mail:jakarta.mail:2.0.1")
    runtimeOnly("org.postgresql:postgresql")

    /* Test dependencies */
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("org.junit.vintage:junit-vintage-engine")
    }
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.ninja-squad:springmockk:3.0.1")
}

sourceSets {
    main {
        java.srcDirs("src/main/java")
        kotlin.srcDirs("src/main/kotlin")
    }
    test {
        java.srcDirs("src/test/java")
        kotlin.srcDirs("src/test/kotlin")
    }
}

application {
//    val ENABLE_PREVIEW = "--enable-preview"
//    applicationDefaultJvmArgs = listOf(ENABLE_PREVIEW)
    mainClass = "org.tctalent.server.TcTalentApplication"
}

jib {
    to {
        if (project.hasProperty("test-tc-system")) {
            image = "231168606641.dkr.ecr.us-east-1.amazonaws.com/test-ecs"
        } else if (project.hasProperty("prod-tc-system")) {
            image = "968457613372.dkr.ecr.us-east-1.amazonaws.com/talent-catalog"
        }
        setCredHelper("ecr-login")
    }
}

tasks.bootJar {
    from(".") {
        include(".ebextensions/**")
    }
}

tasks {
//    val ENABLE_PREVIEW = "--enable-preview"
    withType<JavaCompile>() {
//        options.compilerArgs.add(ENABLE_PREVIEW)
//        options.compilerArgs.add("-Xlint:preview")
    }
    withType<Test>() {
        useJUnitPlatform()
//        jvmArgs(ENABLE_PREVIEW)
    }
    withType<JavaExec>() {
//        jvmArgs(ENABLE_PREVIEW)
    }
}