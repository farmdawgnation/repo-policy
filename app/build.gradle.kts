plugins {
    `java`
    `application`
    kotlin("jvm")
}

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
