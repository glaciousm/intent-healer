plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.16.1"
}

group = "com.intenthealer"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

intellij {
    version.set("2023.2")
    type.set("IC") // IntelliJ IDEA Community Edition
    plugins.set(listOf("java", "Gherkin", "cucumber-java"))
}

dependencies {
    implementation(project(":healer-core"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
}

tasks {
    patchPluginXml {
        version.set("${project.version}")
        sinceBuild.set("232")
        untilBuild.set("242.*")
        changeNotes.set("""
            <h3>1.0.0</h3>
            <ul>
                <li>Initial release</li>
                <li>Heal history viewer</li>
                <li>Trust level dashboard</li>
                <li>Quick actions for heal management</li>
            </ul>
        """.trimIndent())
    }

    buildSearchableOptions {
        enabled = false
    }

    runIde {
        jvmArgs("-Xmx2g")
    }
}
