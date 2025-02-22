plugins {
    java
    alias(libs.plugins.internalConvention)
}

dependencies {
    testImplementation(libs.toolisticon.cute)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.platform.launcher)
}
