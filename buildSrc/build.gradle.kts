/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */
/**
 * Following recommendations here: 
 * https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin-dsl_plugin
 * and
 * https://quickbirdstudios.com/blog/gradle-kotlin-buildsrc-plugin-android/
 */
plugins {
    `kotlin-dsl`
    java
    id("com.github.node-gradle.node") version "2.2.4"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    /* Depend on the default Gradle API's since we want to build a custom plugin */
    implementation(gradleApi())
    implementation(localGroovy())
}