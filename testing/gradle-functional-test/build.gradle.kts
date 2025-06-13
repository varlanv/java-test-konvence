plugins {
    `java-gradle-plugin`
    alias(libs.plugins.internalConvention)
    alias(libs.plugins.testKonvenceSelf)
}

internalConvention {
    integrationTestName = "functionalTest"
}

dependencies {
    implementation(projects.lib.gradlePlugin)
    implementation(projects.lib.annotationProcessor)
    testImplementation(libs.apache.commons.lang)
    testImplementation(gradleTestKit())
}
