plugins {
    `java-library`
    application
}

application {
    mainClass.set("com.intenthealer.cli.HealerCli")
}

dependencies {
    implementation(project(":healer-core"))
    implementation(project(":healer-report"))

    testImplementation(libs.bundles.testing)
}
