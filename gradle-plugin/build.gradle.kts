plugins {
    java
    alias(libs.plugins.internalConvention)
}

gradlePlugin {
    plugins {
        create("testKonvenceGradlePlugin") {
            id = "com.varlanv.testkonvence-gradle-plugin"
            implementationClass = "com.varlanv.testkonvence.gradle.plugin.TestKonvencePlugin"
        }
    }
}

dependencies {
    implementation(projects.annotationProcessor)
    implementation(projects.testEnforce)
}
