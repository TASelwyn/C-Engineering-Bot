import org.gradle.api.JavaVersion.VERSION_21

plugins {
    application
}

group = "wtf.devil.cengbot"
version = "0.0.4-DEV"
description = "C-Eng Bot by Devil"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    targetCompatibility = VERSION_21
    sourceCompatibility = VERSION_21
}

// Javacord is on Maven central
repositories {
    mavenCentral()
}

// The dependencies of the bot. Javacord and Log4J for logging
dependencies {
    implementation("org.javacord:javacord:3.8.0")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.xerial:sqlite-jdbc:3.51.1.0")
    // Logging
    implementation("org.apache.logging.log4j:log4j-api:2.25.3")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.25.3")
}

application {
    // Required to move the output scripts to the root folder
    executableDir = ""
    mainClass.set("wtf.devil.cengbot.Main")
}
