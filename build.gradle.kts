tasks.register("buildServerCore") {
    dependsOn(":server:build")
}

// called by Heroku by default
tasks.register("dependsOn") {
    dependsOn(":buildServerCore")
}
