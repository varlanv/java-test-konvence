plugins {
    java
    alias(libs.plugins.internalConvention)
}

dependencies {
    implementation(projects.annotationProcessor)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.platform.launcher)
}
