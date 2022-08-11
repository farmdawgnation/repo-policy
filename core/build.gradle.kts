plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
    jacoco
}

dependencies {
    implementation("com.charleskorn.kaml:kaml:0.26.0")
    implementation("org.kohsuke:github-api:1.129")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    compileOnly("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
    testImplementation("io.mockk:mockk:1.10.6")
}

tasks.test {
    // Use junit platform for unit tests.
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        csv.isEnabled = false
        html.destination = layout.buildDirectory.dir("jacocoHtml").get().asFile
    }
}
