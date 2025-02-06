plugins {
    java
    alias(libs.plugins.internalConvention)
}

dependencies {
    testAnnotationProcessor(projects.annotationProcessor)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.platform.launcher)
}
