import org.gradle.api.JavaVersion.VERSION_25

plugins {
    application
}

group = "wtf.devil.cengbot"
version = "0.0.7-DEV"
description = "C-Eng Bot by Devil"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    targetCompatibility = VERSION_25
    sourceCompatibility = VERSION_25
}

// JDA is on Maven central
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/TASelwyn/Wooclapper")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

// The dependencies of the bot. JDA and Log4J for logging
dependencies {
    implementation("net.dv8tion:JDA:6.7.0")
    implementation("com.google.code.gson:gson:2.13.2")
    // Database: Hibernate ORM over Postgres, pooled with HikariCP
    implementation("org.hibernate.orm:hibernate-core:7.4.11.Final")
    implementation("org.hibernate.orm:hibernate-hikaricp:7.4.11.Final")
    runtimeOnly("org.postgresql:postgresql:42.7.13")
    // Lombok (compile-time only)
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")
    //implementation("io.lettuce:lettuce-core:7.2.1.RELEASE")
    // Logging
    implementation("org.apache.logging.log4j:log4j-api:2.25.3")
    implementation("org.apache.logging.log4j:log4j-core:2.25.4")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1")

    // Wooclap
    implementation("tech.selwyn:wooclapper:1.0.14") {
        // Wooclapper ships slf4j-simple, which would steal its logs away from log4j (and the Discord log router)
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }
}

application {
    // Required to move the output scripts to the root folder
    executableDir = ""
    mainClass.set("wtf.devil.cengbot.DevilsBot")
}
