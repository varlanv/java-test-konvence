package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.Constants;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;

import java.nio.file.Path;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class TestKonvencePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().withId("java", javaPlugin -> {
            project.getLogger().lifecycle("Hello!");
            val extensions = project.getExtensions();
            val objects = project.getObjects();
            val javaExtension = extensions.getByType(JavaPluginExtension.class);
            val javaSourceSets = javaExtension.getSourceSets();
            val testSourceSet = javaSourceSets.getByName("test");
            val compileClasspath = testSourceSet.getCompileClasspath();
            val tasks = project.getTasks();
            val layout = project.getLayout();
            val buildDirectory = layout.getBuildDirectory();
            val dependencies = project.getDependencies();
            dependencies.add(JavaPlugin.TEST_ANNOTATION_PROCESSOR_CONFIGURATION_NAME, "org.junit.jupiter:junit-jupiter-api:5.11.3");
            val pluginDirPathProvider = buildDirectory.map(dir -> dir.getAsFile().toPath().resolve("tmp").resolve("testkonvenceplugin").toAbsolutePath());
            new ConfigureOnBeforeCompileTestStart(pluginDirPathProvider).setupAnnotationProcessorJar();

            tasks.withType(Test.class).configureEach(test -> {
                val testTaskName = test.getName();
                Optional.ofNullable(tasks.findByName("compile" + capitalize(testTaskName) + "Java"))
                    .map(JavaCompile.class::cast)
                    .ifPresent(compileTestJava -> {
                        val enforceFilesCollection = objects.fileCollection();
                        enforceFilesCollection.setFrom(
                            buildDirectory.map(buildDir -> buildDir
                                .getAsFileTree()
                                .matching(pattern -> pattern.include("generated/sources/annotationProcessor/**/testkonvence_enforcements.xml"))
                            )
                        );
                        val compileClasspathCollection = objects.fileCollection();
                        compileClasspathCollection.setFrom(compileClasspath);
                        val sourcesRootProp = objects.fileProperty().fileProvider(
                            project.provider(
                                () -> testSourceSet.getJava().getSrcDirs().iterator().next()
                            )
                        );
                        test.doLast(
                            TestNameEnforceAction.name(),
                            new TestNameEnforceAction(
                                sourcesRootProp,
                                compileClasspathCollection,
                                enforceFilesCollection
                            )
                        );

                        val options = compileTestJava.getOptions();
                        val processorJar = buildDirectory.files("tmp/testkonvenceplugin/" + Constants.PROCESSOR_JAR);
                        val annotationProcessorClasspath = Optional.ofNullable(options.getAnnotationProcessorPath())
                            .map(f -> f.plus(processorJar))
                            .orElse(processorJar);
                        options.setAnnotationProcessorPath(annotationProcessorClasspath);
                        compileTestJava.doFirst(
                            new ConfigureOnBeforeCompileTestStart(
                                buildDirectory.map(dir -> dir.getAsFile().toPath().resolve("tmp").resolve("testkonvenceplugin").toAbsolutePath())
                            )
                        );
                    });
            });
        });
    }

    private String capitalize(String string) {
        val chars = string.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
}
