import com.moowork.gradle.node.npm.NpmTask

//todo Note that candidate-portal and admin-portal build.gradle files
//are now identical. We should be able to eliminate that duplication.
//Thinking buildSrc and Kotlin

plugins {
  java
  id("com.github.node-gradle.node") version "2.2.4"
  id("org.tbbtalent.talentcatalog")
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

node {
  version = "12.4.0"
  download = true
}

tasks {
  //NpmTask runs an npm command with the given arguments.
  //https://github.com/srs/gradle-node-plugin/blob/master/src/main/groovy/com/moowork/gradle/node/npm/NpmTask.groovy
  //In this case the args specify that the npm run (aka npm run-script) command
  //is executed.
  //See https://docs.npmjs.com/cli/v6/commands/npm-run-script
  //See also https://stackoverflow.com/questions/11580961/sending-command-line-arguments-to-npm-script
  //This runs the Angular script "build" ("ng build" - see Package.json) which
  //Angular includes in the default created package.json.
  //The parameters after the '--' are passed to the Angular build script
  //- ie to ng build
  //See https://angular.io/cli/build
  //So this task effectively runs "ng build --prod --base-href /...-portal/"
  register<NpmTask>("npm_build_ui_bundle") {
    setArgs(listOf("run", "build", "--", "--prod", "--base-href", "/${project.name}/"))
    dependsOn("npm_install")
  }

  //This creates a jar which when unpacked has all the Angular stuff in a
  //subdirectory called ui-bundle.
  //Both portals will have their classes in that subdirectory.
  //The Spring Server WebConfiguration class serves up that Angular stuff from
  //that ui-bundle subdirectory directory.
  jar {
    from("dist")
    into("ui-bundle")
    dependsOn("npm_build_ui_bundle")
  }

}

