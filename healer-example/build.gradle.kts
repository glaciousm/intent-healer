plugins {
    java
}

dependencies {
    implementation(project(":healer-core"))
    implementation(project(":healer-llm"))
    implementation(project(":healer-selenium"))
    implementation(project(":healer-cucumber"))
    implementation(project(":healer-report"))

    implementation("io.cucumber:cucumber-java:7.15.0")
    implementation("io.cucumber:cucumber-junit-platform-engine:7.15.0")
    implementation("org.seleniumhq.selenium:selenium-java:4.16.1")
    implementation("org.junit.platform:junit-platform-suite:1.10.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
}
