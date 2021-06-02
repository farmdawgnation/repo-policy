plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation("com.charleskorn.kaml:kaml:0.26.0")
    implementation("org.kohsuke:github-api:1.129")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    testImplementation("io.mockk:mockk:1.10.6")
}

tasks.test {
    // Use junit platform for unit tests.
    useJUnitPlatform()
}
