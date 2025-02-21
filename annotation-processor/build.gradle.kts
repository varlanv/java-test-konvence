plugins {
    java
    alias(libs.plugins.internalConvention)
}

dependencies {
    compileOnly(libs.junit.jupiter.api)
    testImplementation(libs.toolisticon.cute)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.platform.launcher)
}
