plugins {
    `java-gradle-plugin`
    alias(libs.plugins.internalConvention)
    alias(libs.plugins.testKonvenceSelf)
}

internalConvention {
    integrationTestName = "functionalTest"
}

dependencies {
    implementation(projects.gradlePlugin)
    implementation(projects.annotationProcessor)
    testImplementation(libs.apache.commons.lang)
    testImplementation(gradleTestKit())
}
