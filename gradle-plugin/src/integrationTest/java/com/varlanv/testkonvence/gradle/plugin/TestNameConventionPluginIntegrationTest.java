package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testnameconvention.commontest.GradleIntegrationTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.assertj.core.api.Assertions;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

public class TestNameConventionPluginIntegrationTest implements GradleIntegrationTest {

    @Test
    void asd() {
        runProjectFixture(fixture -> {
            var project = fixture.project();
            var annotationProcessorOutputDir = Files.createDirectories(fixture.projectDir().resolve("build").resolve("generated").resolve("sources").resolve("annotationProcessor").resolve("java").resolve("main"));
            var enforceXml = Files.writeString(annotationProcessorOutputDir.resolve("testkonvence_enforcements.xml"), "", StandardOpenOption.CREATE_NEW);
            var packageDir = Files.createDirectories(fixture.projectDir().resolve("src").resolve("main").resolve("java").resolve("testcase"));
            var javaFile = Files.createFile(packageDir.resolve("SomeSourceFile.java"));
            Files.writeString(javaFile, "package testcase;\n\npublic class SomeSourceFile {}");
            project.getPlugins().apply(TestNameConventionPlugin.class);
            JavaPlugin apply = project.getPlugins().apply(JavaPlugin.class);
            var enforceTask = (TestNameEnforceTask) project.getTasks().getByName(TestNameEnforceTask.name());
            var testTask = project.getTasks().getByName(JavaPlugin.TEST_TASK_NAME);
            var javaCompile = (JavaCompile) project.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME);
            evaluateProject(project);

            System.out.println(
                FileUtils.listFilesAndDirs(
                        fixture.projectDir().toFile(),
                        TrueFileFilter.INSTANCE,
                        TrueFileFilter.INSTANCE
                    ).stream()
                    .map(it -> it.getAbsolutePath() + System.lineSeparator())
                    .collect(Collectors.joining())
            );
            System.out.println(javaCompile.getClasspath().getFiles());
            System.out.println(enforceTask.getCompileClasspath().getFiles());
            System.out.println("Java sources root -> " +enforceTask.getSourcesRoot().getAsFile().get().getPath());
            var resolvedEnforceXml = enforceTask.getEnforceFiles().getSingleFile();
            System.out.println(resolvedEnforceXml);
            assertThat(resolvedEnforceXml).exists();

            enforceTask.enforce();
        });
    }
}
