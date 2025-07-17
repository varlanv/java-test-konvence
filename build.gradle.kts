import kotlin.io.path.readText
import kotlin.io.path.writeText

tasks.named<UpdateDaemonJvm>("updateDaemonJvm") {
    languageVersion = JavaLanguageVersion.of(21)
    vendor = JvmVendorSpec.ADOPTIUM
}

abstract class IncrementVersion : DefaultTask() {

    @InputDirectory
    abstract fun getRootProjectFile(): DirectoryProperty

    @Input
    abstract fun getVersionSemantic(): Property<String>

    @Input
    abstract fun getCurrentVersion(): Property<String>

    @TaskAction
    fun run() {
        val versionSemantic = getVersionSemantic().get()
        val rootProjectPath = getRootProjectFile().get().asFile.toPath()
        val currentVersion = getCurrentVersion().get()
        val currentVersionParts = currentVersion.split('.')
        val newVersion = when (versionSemantic) {
            "Patch" -> "${currentVersionParts[0]}.${currentVersionParts[1]}.${Integer.valueOf(currentVersionParts[2]) + 1}"
            "Minor" -> "${currentVersionParts[0]}.${Integer.valueOf(currentVersionParts[1]) + 1}.0"
            "Major" -> "${Integer.valueOf(currentVersionParts[0]) + 1}.0.0"
            else -> throw IllegalStateException("Unknown version semantic -> $versionSemantic")
        }

        listOf(
            rootProjectPath
                .resolve("lib")
                .resolve("constants")
                .resolve("src")
                .resolve("main")
                .resolve("java")
                .resolve("com")
                .resolve("varlanv")
                .resolve("testkonvence")
                .resolve("Constants.java"),
            rootProjectPath.resolve("gradle.properties"),
            rootProjectPath.resolve("gradle").resolve("libs.versions.toml"),
        ).forEach {
            val text = it.readText(Charsets.UTF_8)
            val firstIndexOfVersion = text.indexOf(currentVersion)
            if (firstIndexOfVersion == -1) {
                throw IllegalStateException("Version $currentVersion not found in file -> $it")
            }
            val lastIndexOfVersion = text.lastIndexOf(currentVersion)
            if (firstIndexOfVersion != lastIndexOfVersion) {
                throw IllegalStateException("Multiple occurrences of version $currentVersion in file -> $it")
            }
            val newText = text.replace(currentVersion, newVersion)
            if (text != newText) {
                it.writeText(newText, Charsets.UTF_8)
            }
        }
    }
}

listOf("Patch", "Minor", "Major").forEach {
    tasks.register("increment${it}Version", IncrementVersion::class) {
        group = "version"
        getRootProjectFile().set(project.layout.projectDirectory)
        getVersionSemantic().set(it)
        getCurrentVersion().set(properties["version"].toString())
    }
}
