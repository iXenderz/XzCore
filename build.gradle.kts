plugins {
    id("java-library")
    id("com.gradleup.shadow") version "8.3.5"
    id("maven-publish")
}

group = "com.xzatrix"
version = "1.0.0"

repositories {
    mavenCentral()
    maven(url = "https://repo.papermc.io/repository/maven-public/")
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    
    // HikariCP for connection pooling
    api("com.zaxxer:HikariCP:6.2.1")
    
    // SQLite JDBC
    api("org.xerial:sqlite-jdbc:3.47.1.0")
    
    // MySQL Connector (optional)
    api("com.mysql:mysql-connector-j:9.1.0")
    
    // Adventure API (for components)
    compileOnly("net.kyori:adventure-api:4.17.0")
    
    // JUnit 5 for testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testImplementation("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks {
    compileJava {
        options.release = 21
        options.compilerArgs.add("-parameters")
    }
    
    test {
        useJUnitPlatform()
    }
    
    shadowJar {
        archiveClassifier.set("")
        // HikariCP and SQLite are API dependencies - don't shade them
        // Let consuming plugins shade if needed
    }
    
    build {
        dependsOn(shadowJar)
    }
    
    javadoc {
        options {
            encoding = "UTF-8"
            (this as StandardJavadocDocletOptions).tags(
                "apiNote:a:API Note:",
                "implSpec:a:Implementation Requirements:",
                "implNote:a:Implementation Note:"
            )
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "com.xzatrix"
            artifactId = "xzcore"
            version = project.version.toString()
            
            // Publish the shadow JAR as the main artifact
            artifact(tasks["shadowJar"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            
            pom {
                name.set("XzCore")
                description.set("Core API and services for XzPlugin suite")
                url.set("https://github.com/iXenderz/XzPlugin")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("xzatrix")
                        name.set("Xzatrix")
                    }
                }
            }
        }
    }
}
