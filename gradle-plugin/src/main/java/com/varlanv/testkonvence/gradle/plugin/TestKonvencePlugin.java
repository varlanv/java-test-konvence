package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.Constants;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.testing.base.TestingExtension;

@SuppressWarnings("UnstableApiUsage")
public class TestKonvencePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        val testKonvenceTaskGroup = "test konvence";
        val testKonvenceEnforceAllTaskName = "testKonvenceEnforceAll";
        val testKonvenceDryEnforceWithFailingTaskName = "testKonvenceDryEnforceWithFailing";
        val extensions = project.getExtensions();
        val dependencies = project.getDependencies();
        val testKonvenceExtension = (TestKonvenceExtension) extensions.create(
                TestKonvenceExtensionView.class, TestKonvenceExtensionView.name(), TestKonvenceExtension.class);
        val tasks = project.getTasks();
        val providers = project.getProviders();

        project.afterEvaluate(p -> {
            project.getPlugins().withId("java", javaPlugin -> {
                if (testKonvenceExtension.getEnabled().get()) {
                    val testKonvenceEnforceAllTask =
                            tasks.register(testKonvenceEnforceAllTaskName, testKonvenceEnforceAll -> {
                                testKonvenceEnforceAll.getOutputs().upToDateWhen(ignore -> false);
                                testKonvenceEnforceAll.setGroup(testKonvenceTaskGroup);
                                testKonvenceEnforceAll.dependsOn(tasks.withType(JavaCompile.class));
                            });
                    val testKonvenceDryEnforceWithFailingTask =
                            tasks.register(testKonvenceDryEnforceWithFailingTaskName, testKonvenceEnforceAll -> {
                                testKonvenceEnforceAll.getOutputs().upToDateWhen(ignore -> false);
                                testKonvenceEnforceAll.setGroup(testKonvenceTaskGroup);
                                testKonvenceEnforceAll.dependsOn(tasks.withType(JavaCompile.class));
                            });

                    val log = project.getLogger();
                    val objects = project.getObjects();
                    val layout = project.getLayout();
                    val buildDirectory = layout.getBuildDirectory();
                    val testing = (TestingExtension) extensions.getByName("testing");
                    val annotationProcessorTargetPathProvider = buildDirectory.map(dir -> dir.getAsFile()
                            .toPath()
                            .resolve("tmp")
                            .resolve("testkonvenceplugin")
                            .resolve(Constants.PROCESSOR_JAR));
                    val setupAnnotationProcessorTaskProvider =
                            tasks.register("setupTestKonvenceAnnotationProcessor", task -> {
                                task.doLast(
                                        new ConfigureOnBeforeCompileTestStart(annotationProcessorTargetPathProvider));
                                task.getOutputs().file(annotationProcessorTargetPathProvider);
                            });

                    testing.getSuites().configureEach(testSuite -> {
                        if (testSuite instanceof JvmTestSuite) {
                            val jvmTestSuite = (JvmTestSuite) testSuite;
                            val testSourceSet = jvmTestSuite.getSources();
                            // configure compile task to use annotation processor
                            tasks.named(testSourceSet.getCompileJavaTaskName(), JavaCompile.class)
                                    .configure(compileTestJava -> {
                                        compileTestJava.dependsOn(setupAnnotationProcessorTaskProvider);
                                        compileTestJava.mustRunAfter(setupAnnotationProcessorTaskProvider);
                                    });
                            val name = testSuite.getName();
                            val processorJar =
                                    buildDirectory.files("tmp/testkonvenceplugin/" + Constants.PROCESSOR_JAR);
                            dependencies.add(name + "AnnotationProcessor", processorJar);

                            jvmTestSuite.getTargets().configureEach(testTarget -> {
                                val testTask = testTarget.getTestTask();
                                log.debug("Configuring test task [{}]", testTask.getName());
                                val enforceFilesCollection = objects.fileCollection();
                                enforceFilesCollection.setFrom(
                                        buildDirectory.map(
                                                buildDir -> buildDir.getAsFileTree()
                                                        .matching(
                                                                pattern -> pattern.include(
                                                                        "generated/sources/annotationProcessor/**/testkonvence_enforcements.xml"))));
                                val compileClasspath = objects.fileCollection();
                                compileClasspath.setFrom(testSourceSet.getCompileClasspath());
                                val testNameEnforceAction = new TestNameEnforceAction(
                                        objects.fileCollection().from(project.provider(() -> testSourceSet
                                                .getJava()
                                                .getSrcDirs()
                                                .iterator()
                                                .next())),
                                        compileClasspath,
                                        enforceFilesCollection,
                                        testKonvenceExtension.getApplyAutomaticallyAfterTestTask(),
                                        testKonvenceExtension.getCamelCaseMethodNameProperty(),
                                        testKonvenceExtension
                                                .getReverseTransformation()
                                                .get()
                                                .getEnabled());
                                val enforceTaskProvider = tasks.register(
                                        TestNameEnforceTask.name(testTask.getName()),
                                        TestNameEnforceTask.class,
                                        enforceTask -> {
                                            enforceTask
                                                    .getSourcesRootProp()
                                                    .setFrom(testNameEnforceAction.sourcesRootProp());
                                            enforceTask
                                                    .getCompileClasspath()
                                                    .setFrom(testNameEnforceAction.compileClasspath());
                                            enforceTask.getEnforceFiles().setFrom(testNameEnforceAction.enforceFiles());
                                            enforceTask
                                                    .getUseCamelCaseMethodName()
                                                    .set(testNameEnforceAction.camelCaseMethodNameProvider());
                                            enforceTask
                                                    .getEnableReverseTransformation()
                                                    .set(testNameEnforceAction.enableReverseTransformation());
                                        });
                                if (testKonvenceExtension
                                        .getApplyAutomaticallyAfterTestTask()
                                        .get()) {
                                    testTask.configure(test -> {
                                        test.finalizedBy(enforceTaskProvider);
                                    });
                                }

                                testKonvenceEnforceAllTask.configure(task -> task.doLast(
                                        newEnforceAction(testNameEnforceAction, providers.provider(() -> false))));
                                testKonvenceDryEnforceWithFailingTask.configure(task -> task.doLast(
                                        newEnforceAction(testNameEnforceAction, providers.provider(() -> true))));
                            });
                        }
                    });
                }
            });
        });
    }

    private TestNameEnforceAction newEnforceAction(
            TestNameEnforceAction testNameEnforceAction, Provider<Boolean> dryWithFailingProvider) {
        return new TestNameEnforceAction(
                testNameEnforceAction.sourcesRootProp(),
                testNameEnforceAction.compileClasspath(),
                testNameEnforceAction.enforceFiles(),
                dryWithFailingProvider,
                testNameEnforceAction.camelCaseMethodNameProvider(),
                testNameEnforceAction.enableReverseTransformation());
    }
}
