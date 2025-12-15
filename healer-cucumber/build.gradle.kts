plugins {
    `java-library`
}

dependencies {
    api(project(":healer-core"))
    api(project(":healer-llm"))
    api(project(":healer-selenium"))
    api(libs.bundles.cucumber)
    api(libs.bundles.selenium)

    testImplementation(libs.bundles.testing)
}
