package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.Constants;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.testing.base.TestingExtension;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class TestKonvencePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().withId("java", javaPlugin -> {
            val extensions = project.getExtensions();
            val objects = project.getObjects();
            val tasks = project.getTasks();
            val layout = project.getLayout();
            val buildDirectory = layout.getBuildDirectory();
            val dependencies = project.getDependencies();
            val testing = (TestingExtension) extensions.getByName("testing");
            dependencies.add(JavaPlugin.TEST_ANNOTATION_PROCESSOR_CONFIGURATION_NAME, "org.junit.jupiter:junit-jupiter-api:5.11.3");
            val annotationProcessorTargetPathProvider = buildDirectory.map(dir ->
                dir.getAsFile().toPath().resolve("tmp").resolve("testkonvenceplugin").resolve(Constants.PROCESSOR_JAR)
            );
            val setupAnnotationProcessorTaskProvider = tasks.register(
                "setupTestKonvenceAnnotationProcessor",
                task -> {
                    task.doLast(new ConfigureOnBeforeCompileTestStart(annotationProcessorTargetPathProvider));
                    task.getOutputs().file(annotationProcessorTargetPathProvider);
                }
            );
            val testKonvenceTaskGroup = "test konvence";
            tasks.register("testKonvenceEnforceAll").configure(testKonvenceEnforceAll -> {
                testKonvenceEnforceAll.getOutputs().upToDateWhen(ignore -> false);
                testKonvenceEnforceAll.setGroup(testKonvenceTaskGroup);
                testKonvenceEnforceAll.dependsOn(tasks.withType(JavaCompile.class));
                tasks.named(taskName -> taskName.contains("KonvenceEnforce") && !taskName.endsWith("All"))
                    .forEach(testKonvenceEnforceAll::finalizedBy);
            });
            tasks.register("testKonvenceDryEnforceWithFailing").configure(testKonvenceEnforceAll -> {
                testKonvenceEnforceAll.getOutputs().upToDateWhen(ignore -> false);
                testKonvenceEnforceAll.setGroup(testKonvenceTaskGroup);
                testKonvenceEnforceAll.dependsOn(tasks.withType(JavaCompile.class));
                tasks.named(taskName -> taskName.contains("KonvenceEnforce") && !taskName.endsWith("All"))
                    .stream()
                    .filter(TestNameEnforceTask.class::isInstance)
                    .map(TestNameEnforceTask.class::cast)
                    .forEach(enforceTask -> {
                        testKonvenceEnforceAll.finalizedBy(enforceTask);
                        enforceTask.getDryWithFailing().set(true);
                    });
            });
            testing.getSuites().configureEach(suite -> {
                if (suite instanceof JvmTestSuite) {
                    val jvmSuite = (JvmTestSuite) suite;
                    jvmSuite.sources(testSourceSet -> {
                        tasks.named(taskName -> taskName.equals(testSourceSet.getCompileJavaTaskName())).configureEach(unsafeCompileTestJava -> {
                            val compileTestJava = (JavaCompile) unsafeCompileTestJava;
                            compileTestJava.dependsOn(setupAnnotationProcessorTaskProvider);
                            compileTestJava.mustRunAfter(setupAnnotationProcessorTaskProvider);
                            val options = compileTestJava.getOptions();
                            val processorJar = buildDirectory.files("tmp/testkonvenceplugin/" + Constants.PROCESSOR_JAR);
                            val annotationProcessorClasspath = Optional.ofNullable(options.getAnnotationProcessorPath())
                                .map(f -> f.plus(processorJar))
                                .orElse(processorJar);
                            options.setAnnotationProcessorPath(annotationProcessorClasspath);
                        });
                        jvmSuite.getTargets().configureEach(target -> {
                            val testTask = target.getTestTask();
                            val enforceTaskProvider = tasks.register(
                                TestNameEnforceTask.name(testTask.getName()),
                                TestNameEnforceTask.class,
                                enforceTask -> {
                                    enforceTask.getDryWithFailing().convention(false);
                                    enforceTask.getSourcesRootProp().setFrom(objects.fileCollection().from(
                                        project.provider(
                                            () -> testSourceSet.getJava().getSrcDirs().iterator().next()
                                        )
                                    ));
                                    val enforceFilesCollection = objects.fileCollection();
                                    enforceFilesCollection.setFrom(
                                        buildDirectory.map(buildDir -> buildDir
                                            .getAsFileTree()
                                            .matching(pattern ->
                                                pattern.include("generated/sources/annotationProcessor/**/testkonvence_enforcements.xml")
                                            )
                                        )
                                    );
                                    enforceTask.getCompileClasspath().setFrom(testSourceSet.getCompileClasspath());
                                    enforceTask.getEnforceFiles().setFrom(enforceFilesCollection);
//                                    enforceTask.getEnforceFilesCollection().setFrom(enforceFilesCollection);
                                });
                            testTask.configure(test -> {
                                test.finalizedBy(enforceTaskProvider);
                            });
                        });
                    });
                }
            });
        });
    }
}
