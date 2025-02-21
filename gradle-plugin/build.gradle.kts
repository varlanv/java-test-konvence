import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.IncludeResourceTransformer

plugins {
    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.internalConvention)
    id("com.gradleup.shadow") version "8.3.6"
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
    implementation(projects.testEnforce)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier = ""
    dependsOn(":annotation-processor:jar")
    transform(IncludeResourceTransformer::class.java) {
        resource = "annotation-processor-${providers.gradleProperty("version").get()}.jar"
        file = project.rootDir.toPath()
            .resolve("annotation-processor")
            .resolve("build")
            .resolve("libs")
            .resolve("annotation-processor-${providers.gradleProperty("version").get()}.jar")
            .toFile()
    }
}

tasks.named<Jar>("jar") {
    dependsOn(":annotation-processor:jar")
    from(project.rootDir.toPath()
        .resolve("annotation-processor")
        .resolve("build")
        .resolve("libs")
        .resolve("annotation-processor-${providers.gradleProperty("version").get()}.jar"))
}

publishing {
    publications {
        create<MavenPublication>("shadow") {
            from(components["shadow"])
        }
    }
}