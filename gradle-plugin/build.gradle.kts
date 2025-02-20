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
    compileOnly(projects.constants)
    implementation(projects.annotationProcessor)
    implementation(projects.testEnforce)
}

tasks.named<Jar>("jar") {
    dependsOn(":annotation-processor:jar")
    from(project.rootDir.toPath()
        .resolve("annotation-processor")
        .resolve("build")
        .resolve("libs")
        .resolve("annotation-processor-${providers.gradleProperty("version").get()}.jar"))
}
