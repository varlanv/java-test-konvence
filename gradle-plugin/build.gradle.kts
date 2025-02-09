plugins {
    java
    alias(libs.plugins.internalConvention)
}

gradlePlugin {
    plugins {
        create("testNameConventionGradlePlugin") {
            id = "com.varlanv.testnameconvention-gradle-plugin"
            implementationClass = "com.varlanv.testnameconvention.gradle.plugin.TestNameConventionPlugin"
        }
    }
}

dependencies {
    implementation(projects.annotationProcessor)
    implementation(projects.testEnforce)
}
