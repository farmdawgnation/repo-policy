/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("me.frmr.github.kotlin-application-conventions")
}

dependencies {
    implementation(project(":utilities"))
}

application {
    // Define the main class for the application.
    mainClass.set("me.frmr.github.app.AppKt")
}
