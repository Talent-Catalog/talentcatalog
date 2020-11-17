
val isDev: Boolean by extra(System.getProperty("env") == "dev")

plugins {
    java
    idea
    id("org.springframework.boot") version "2.4.0"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

//Yes - you do have to declare this again (even though it is declared in the
//buildscript above) 
//- see https://stackoverflow.com/questions/13923766/gradle-buildscript-dependencies
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    if (!isDev) {
        implementation(project(":ui:admin-portal"))
        implementation(project(":ui:candidate-portal"))
    }
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    implementation(group = "org.apache.pdfbox", name = "pdfbox", version = "2.0.1")
    implementation("com.itextpdf:kernel:7.0.2")
    implementation(group = "org.apache.poi", name = "poi", version = "4.1.2")
    implementation(group = "org.apache.poi", name = "poi-ooxml", version = "4.1.2")
    implementation(group = "org.apache.poi", name = "poi-scratchpad", version = "4.1.2")
    implementation(group = "org.bouncycastle", name = "bcprov-jdk16", version = "1.45")
// https://mvnrepository.com/artifact/org.jdom/jdom2
    implementation(group = "org.jdom", name = "jdom2", version = "2.0.6")
// https://mvnrepository.com/artifact/mysql/mysql-connector-java
    implementation(group = "mysql", name = "mysql-connector-java", version = "5.1.31")
// https://mvnrepository.com/artifact/de.codecentric/spring-boot-admin-starter-client
    implementation(group = "de.codecentric", name = "spring-boot-admin-starter-client", version = "2.2.3")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    //See https://mvnrepository.com/artifact/org.springframework.data/spring-data-elasticsearch
    implementation("org.springframework.data:spring-data-elasticsearch")

    implementation("com.google.guava:guava:23.5-jre")
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    implementation("com.google.api-client:google-api-client:1.30.10")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.31.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev197-1.25.0")
    implementation("org.flywaydb:flyway-core")
    implementation("org.apache.commons:commons-lang3")
    implementation("com.amazonaws:aws-java-sdk-s3:1.11.656")
    implementation("javax.mail:javax.mail-api")
    implementation("com.sun.mail:jakarta.mail")
    implementation("com.opencsv:opencsv:5.0")
    implementation("commons-beanutils:commons-beanutils:1.9.3")
    implementation("org.thymeleaf.extras:thymeleaf-extras-java8time")
    implementation("org.xhtmlrenderer:flying-saucer-core:9.1.6")
    implementation("org.xhtmlrenderer:flying-saucer-pdf-itext5:9.1.6")
    implementation("net.sf.jtidy:jtidy:r938")
    
    // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-impl
    runtimeOnly( group = "io.jsonwebtoken", name = "jjwt-impl", version = "0.11.2")
    // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-jackson
    runtimeOnly(group = "io.jsonwebtoken", name = "jjwt-jackson", version = "0.11.2")
    
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("mysql:mysql-connector-java")
    runtimeOnly("com.h2database:h2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.bootJar {
    from(".") {
        include(".ebextensions/**")
    }
}
