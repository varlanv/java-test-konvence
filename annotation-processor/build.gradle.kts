plugins {
    java
    alias(libs.plugins.internalConvention)
}

dependencies {
    implementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.platform.launcher)
}
