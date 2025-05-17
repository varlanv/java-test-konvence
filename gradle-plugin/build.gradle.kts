plugins {
    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.internalConvention)
//    id("com.gradleup.shadow") version "8.3.6"
}

gradlePlugin {
    plugins {
        create("testKonvenceGradlePlugin") {
            id = "com.varlanv.test-konvence"
            implementationClass = "com.varlanv.testkonvence.gradle.plugin.TestKonvencePlugin"
        }
    }
}

dependencies {
    compileOnly(projects.constants)
    testImplementation(projects.annotationProcessor)
    testImplementation(libs.toolisticon.cute)
}

tasks.named<Jar>("jar") {
    dependsOn(":annotation-processor:shadowJar")
    from(project.rootDir.toPath()
        .resolve("annotation-processor")
        .resolve("build")
        .resolve("libs")
        .resolve("annotation-processor-${providers.gradleProperty("version").get()}-all.jar"))
}
