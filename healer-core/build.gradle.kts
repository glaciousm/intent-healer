plugins {
    `java-library`
}

dependencies {
    api(libs.bundles.jackson)
    api(libs.bundles.logging)
    api(libs.snakeyaml)
    api(libs.guava)
    api(libs.caffeine)
    api(libs.micrometer.core)

    testImplementation(libs.bundles.testing)
}
