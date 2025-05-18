plugins {
    java
    alias(libs.plugins.internalConvention)
}

dependencies {
    compileOnly(projects.constants)
    compileOnly(projects.sharedUtil)
    testImplementation(libs.toolisticon.cute)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.named<Jar>("jar") {
    dependsOn(":shared-util:jar")
    val rootDirPath = project.rootDir.toPath()
    from(rootDirPath
        .resolve("shared-util")
        .resolve("build")
        .resolve("classes")
        .resolve("java")
        .resolve("main"))
}
