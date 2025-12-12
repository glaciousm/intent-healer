plugins {
    `java-library`
}

dependencies {
    api(project(":healer-core"))
    api(libs.bundles.selenium)

    testImplementation(libs.bundles.testing)
}
