plugins {
    `java-library`

    `maven-publish`
}

group = "dev.hollowcube"
version = System.getenv("TAG_VERSION") ?: "dev"
description = "Fast and small world format for Minestom"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly(libs.minestom)
    implementation(libs.zstd)
    // Fastutil is only included because minestom already uses it, otherwise it is a crazy dependency
    // for how it is used in this project.
    implementation(libs.fastutil)

    testImplementation("ch.qos.logback:logback-core:1.4.7")
    testImplementation("ch.qos.logback:logback-classic:1.4.7")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.minestom)
}

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.test {
    maxHeapSize = "2g"
    useJUnitPlatform()
}
