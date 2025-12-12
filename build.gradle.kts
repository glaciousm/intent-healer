plugins {
    java
    `java-library`
    `maven-publish`
}

allprojects {
    group = "com.intenthealer"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }

    dependencies {
        testImplementation(rootProject.libs.bundles.testing)
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                pom {
                    name.set(project.name)
                    description.set("Intent-Based Self-Healing for Selenium + Java + Cucumber")
                    url.set("https://github.com/intenthealer/intent-healer")
                    licenses {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                }
            }
        }
    }
}

// Root project configuration
tasks.register("cleanAll") {
    dependsOn(subprojects.map { it.tasks.named("clean") })
}

tasks.register("buildAll") {
    dependsOn(subprojects.map { it.tasks.named("build") })
}

tasks.register("testAll") {
    dependsOn(subprojects.map { it.tasks.named("test") })
}
