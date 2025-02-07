plugins {
    java
    alias(libs.plugins.internalConvention)
}

dependencies {
    implementation(libs.junit.jupiter.api)
    testImplementation("io.toolisticon.cute:cute:1.7.0")
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.platform.launcher)
}
