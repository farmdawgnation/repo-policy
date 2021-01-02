plugins {
    kotlin("jvm") version "1.4.20"
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("info.picocli:picocli:4.6.0")

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
