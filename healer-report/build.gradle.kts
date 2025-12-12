plugins {
    `java-library`
}

dependencies {
    api(project(":healer-core"))

    testImplementation(libs.bundles.testing)
}
