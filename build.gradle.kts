import org.gradle.api.JavaVersion.VERSION_11

plugins {
    application
}

group = "wtf.devil.cengbot"
version = "0.0.4-DEV"
description = "C-Eng Bot by Devil"

java {
    sourceCompatibility = VERSION_11
}

// Javacord is on Maven central
repositories {
    mavenCentral()
}

// The dependencies of the bot. Javacord and Log4J for logging
dependencies {
    implementation("org.javacord:javacord:3.1.2")
    implementation("org.apache.logging.log4j:log4j-api:2.14.0")
    implementation("com.google.code.gson:gson:2.8.6")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.14.0")
    implementation("org.xerial:sqlite-jdbc:3.34.0")
}

application {
    // Required to move the output scripts to the root folder
    executableDir = ""
    mainClass.set("wtf.devil.cengbot.Main")
}
