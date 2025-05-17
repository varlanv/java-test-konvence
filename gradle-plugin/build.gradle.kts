plugins {
    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.internalConvention)
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
    compileOnly(projects.sharedUtil)
    compileOnly(projects.constants)
    testImplementation(projects.annotationProcessor)
    testImplementation(libs.toolisticon.cute)
}

tasks.named<Jar>("jar") {
    dependsOn(":annotation-processor:jar")
    dependsOn(":shared-util:jar")
    val rootDirPath = project.rootDir.toPath()
    from(rootDirPath
        .resolve("shared-util")
        .resolve("build")
        .resolve("classes")
        .resolve("java")
        .resolve("main"))
    from(rootDirPath
        .resolve("annotation-processor")
        .resolve("build")
        .resolve("libs")
        .resolve("annotation-processor-${providers.gradleProperty("version").get()}.jar"))
}
