plugins {
    `java-library`
}

dependencies {
    api(project(":healer-core"))
    api(libs.okhttp)

    testImplementation(libs.bundles.testing)
}
