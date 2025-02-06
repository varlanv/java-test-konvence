plugins {
    `java-gradle-plugin`
    alias(libs.plugins.internalConvention)
}

internalConvention {
    integrationTestName = "functionalTest"
}

dependencies {
    implementation(projects.testEnforce)
    implementation(projects.gradlePlugin)
    implementation(projects.annotationProcessor)
    testImplementation(libs.apache.commons.lang)
    testImplementation(gradleTestKit())
}
