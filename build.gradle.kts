plugins {
  java
  kotlin("jvm") version "1.4.20" apply false
  kotlin("plugin.serialization") version "1.4.20" apply false
  jacoco
}

repositories {
  jcenter()
}

subprojects {
  apply(plugin = "java")
  dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testImplementation("org.assertj:assertj-core:3.18.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  }

  repositories {
    mavenCentral()
  }
}
