plugins {
    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.internalConvention)
}

gradlePlugin {
    website = "https://github.com/varlanv/java-test-konvence"
    vcsUrl = "https://github.com/varlanv/java-test-konvence"
    plugins {
        create("testKonvenceGradlePlugin") {
            id = "com.varlanv.test-konvence"
            implementationClass = "com.varlanv.testkonvence.gradle.plugin.TestKonvencePlugin"
            displayName = "Test Konvence Plugin"
            description = "Plugin that provides a way to automatically change and enforce test naming"
            tags = listOf("test", "convention", "junit", "naming", "quality")

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
