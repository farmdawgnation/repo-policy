plugins {
    kotlin("jvm") version "1.4.20"
    kotlin("plugin.serialization") version "1.4.20"
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.charleskorn.kaml:kaml:0.26.0")
    implementation("org.kohsuke:github-api:1.117")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testImplementation("org.assertj:assertj-core:3.18.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

repositories {
    // Use JCenter for resolving dependencies.
    jcenter()
}

tasks.test {
    // Use junit platform for unit tests.
    useJUnitPlatform()
}
