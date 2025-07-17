plugins {
    java
    alias(libs.plugins.internalConvention)
    alias(libs.plugins.testKonvenceSelf)
}

dependencies {
    testImplementation(libs.selfie)
}
