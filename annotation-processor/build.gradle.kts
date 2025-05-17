plugins {
    java
    alias(libs.plugins.internalConvention)
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(projects.sharedUtil)
    testImplementation(libs.toolisticon.cute)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.platform.launcher)
}
