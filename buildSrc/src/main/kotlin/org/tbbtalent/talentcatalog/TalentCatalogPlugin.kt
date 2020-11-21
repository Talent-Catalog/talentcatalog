/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.talentcatalog

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.getByType

/**
 * TODO JC Doc
 * @author John Cameron
 */
class TalentCatalogPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.configurePlugins();
        project.configureUI();
    }
}

private fun Project.configurePlugins() {
    plugins.apply("java")
    plugins.apply("com.github.node-gradle.node" )
}

private fun Project.configureUI()  {
    this.extensions.getByType<JavaPluginExtension>().run {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    this.extensions.getByType<com.moowork.gradle.node.NodeExtension>().run {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    for (task in tasks) {
        println("Task $task")
    }
    
} 
