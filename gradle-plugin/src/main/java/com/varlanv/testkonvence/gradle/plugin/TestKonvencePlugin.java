package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.Constants;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.plugins.jvm.JvmTestSuiteTarget;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.testing.base.TestingExtension;

import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class TestKonvencePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().withId("java", javaPlugin -> {
            val extensions = project.getExtensions();
            val objects = project.getObjects();
            val providers = project.getProviders();
            val tasks = project.getTasks();
            val layout = project.getLayout();
            val buildDirectory = layout.getBuildDirectory();
            val testing = (TestingExtension) extensions.getByName("testing");
            val testKonvenceExtension = (TestKonvenceExtensionInternal) extensions.create(
                TestKonvenceExtension.class,
                TestKonvenceExtension.EXTENSION_NAME,
                TestKonvenceExtensionInternal.class
            );
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
            val testKonvenceEnforceAllTaskName = "testKonvenceEnforceAll";
            val testKonvenceDryEnforceWithFailingTaskName = "testKonvenceDryEnforceWithFailing";

            project.afterEvaluate(p -> {
                val testNameEnforceActions = testing.getSuites().stream()
                    .filter(JvmTestSuite.class::isInstance)
                    .map(JvmTestSuite.class::cast)
                    .flatMap(jvmSuite -> {
                        val testSourceSet = jvmSuite.getSources();
                        // configure compile task to use annotation processor
                        tasks.named(testSourceSet.getCompileJavaTaskName(), JavaCompile.class).configure(compileTestJava -> {
                            compileTestJava.dependsOn(setupAnnotationProcessorTaskProvider);
                            compileTestJava.mustRunAfter(setupAnnotationProcessorTaskProvider);
                            val options = compileTestJava.getOptions();
                            val processorJar = buildDirectory.files("tmp/testkonvenceplugin/" + Constants.PROCESSOR_JAR);
                            val annotationProcessorClasspath = Optional.ofNullable(options.getAnnotationProcessorPath())
                                .map(f -> f.plus(processorJar))
                                .orElse(processorJar);
                            options.setAnnotationProcessorPath(annotationProcessorClasspath);
                        });
                        return jvmSuite.getTargets().stream()
                            .map(JvmTestSuiteTarget::getTestTask)
                            .map(testTask -> {
                                val enforceFilesCollection = objects.fileCollection();
                                enforceFilesCollection.setFrom(
                                    buildDirectory.map(buildDir -> buildDir
                                        .getAsFileTree()
                                        .matching(pattern ->
                                            pattern.include("generated/sources/annotationProcessor/**/testkonvence_enforcements.xml")
                                        )
                                    )
                                );
                                val compileClasspath = objects.fileCollection();
                                compileClasspath.setFrom(testSourceSet.getCompileClasspath());
                                val testNameEnforceAction = new TestNameEnforceAction(
                                    objects.fileCollection().from(
                                        project.provider(
                                            () -> testSourceSet.getJava().getSrcDirs().iterator().next()
                                        )
                                    ),
                                    compileClasspath,
                                    enforceFilesCollection,
                                    testKonvenceExtension.getApplyAutomaticallyAfterTestTask()
                                );
                                val enforceTaskProvider = tasks.register(
                                    TestNameEnforceTask.name(testTask.getName()),
                                    TestNameEnforceTask.class,
                                    enforceTask -> {
                                        enforceTask.getSourcesRootProp().setFrom(testNameEnforceAction.sourcesRootProp());
                                        enforceTask.getCompileClasspath().setFrom(testNameEnforceAction.compileClasspath());
                                        enforceTask.getEnforceFiles().setFrom(testNameEnforceAction.enforceFiles());
                                    }
                                );
                                if (testKonvenceExtension.getApplyAutomaticallyAfterTestTask().get()) {
                                    testTask.configure(test -> {
                                        test.finalizedBy(enforceTaskProvider);
                                    });
                                }
                                return testNameEnforceAction;
                            });
                    })
                    .collect(Collectors.toList());

                tasks.register(
                    testKonvenceEnforceAllTaskName,
                    testKonvenceEnforceAll -> {
                        testKonvenceEnforceAll.getOutputs().upToDateWhen(ignore -> false);
                        testKonvenceEnforceAll.setGroup(testKonvenceTaskGroup);
                        testKonvenceEnforceAll.dependsOn(tasks.withType(JavaCompile.class));
                        testNameEnforceActions.forEach(action ->
                            testKonvenceEnforceAll.doLast(
                                new TestNameEnforceAction(
                                    action.sourcesRootProp(),
                                    action.compileClasspath(),
                                    action.enforceFiles(),
                                    providers.provider(() -> false)
                                )
                            ));
                    });
                tasks.register(
                    testKonvenceDryEnforceWithFailingTaskName,
                    testKonvenceEnforceAll -> {
                        testKonvenceEnforceAll.getOutputs().upToDateWhen(ignore -> false);
                        testKonvenceEnforceAll.setGroup(testKonvenceTaskGroup);
                        testKonvenceEnforceAll.dependsOn(tasks.withType(JavaCompile.class));
                        testNameEnforceActions.forEach(action ->
                            testKonvenceEnforceAll.doLast(
                                new TestNameEnforceAction(
                                    action.sourcesRootProp(),
                                    action.compileClasspath(),
                                    action.enforceFiles(),
                                    providers.provider(() -> true)
                                )
                            ));
                    });
            });
        });
    }
}
