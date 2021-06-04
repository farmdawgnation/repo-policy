import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java`
    `application`
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

val versionNumber = System.getenv("BUILD_VERSION") ?: "0.0.0"

version this.versionNumber

dependencies {
    implementation(project(":core"))
    implementation("info.picocli:picocli:4.6.0")
}

tasks.test {
    // Use junit platform for unit tests.
    useJUnitPlatform()
}

application {
    mainClass.set("me.frmr.github.repopolicy.app.MainKt")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("repo-policy")
    }
}

tasks.jar {
    manifest {
        //attributes("App-Version": versionNumber)
    }
}
