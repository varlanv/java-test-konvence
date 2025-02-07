plugins {
    java
    alias(libs.plugins.internalConvention)
}

dependencies {
    testImplementation(projects.annotationProcessor)
    testImplementation(projects.testEnforce)
    testImplementation(libs.junit.jupiter.api)
    testImplementation("io.toolisticon.cute:cute:1.7.0")
    testRuntimeOnly(libs.junit.platform.launcher)
}
