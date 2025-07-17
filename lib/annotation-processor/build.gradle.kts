plugins {
    java
    alias(libs.plugins.internalConvention)
    alias(libs.plugins.testKonvenceSelf)
}

dependencies {
    compileOnly(projects.lib.constants)
    compileOnly(projects.lib.sharedUtil)
    testCompileOnly(projects.lib.constants)
    testImplementation(libs.toolisticon.cute)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.selfie)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.named<Jar>("jar") {
    dependsOn(":lib:shared-util:jar")
    val rootDirPath = project.rootDir.toPath()
    from(rootDirPath
        .resolve("lib")
        .resolve("shared-util")
        .resolve("build")
        .resolve("classes")
        .resolve("java")
        .resolve("main"))
}
