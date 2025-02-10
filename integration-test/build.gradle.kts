plugins {
    java
    alias(libs.plugins.internalConvention)
}

dependencies {
    testImplementation(projects.annotationProcessor)
    testImplementation(projects.testEnforce)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.toolisticon.cute)
    testRuntimeOnly(libs.junit.platform.launcher)
}
