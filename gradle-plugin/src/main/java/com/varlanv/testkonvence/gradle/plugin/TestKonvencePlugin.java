package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.Constants;
import java.util.ArrayList;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.testing.base.TestingExtension;

@SuppressWarnings("UnstableApiUsage")
public final class TestKonvencePlugin implements Plugin<Project> {

    private static final String testKonvenceTaskGroup = "test konvence";
    private static final String testKonvenceEnforceAllTaskName = "testKonvenceEnforceAll";
    private static final String testKonvenceDryEnforceWithFailingTaskName = "testKonvenceDryEnforceWithFailing";

    @Override
    public void apply(Project project) {
        var extensions = project.getExtensions();
        var dependencies = project.getDependencies();
        var objects = project.getObjects();
        var tasks = project.getTasks();
        var providers = project.getProviders();
        var testKonvenceExtension = (TestKonvenceExtension) extensions.create(
                TestKonvenceExtensionView.class, TestKonvenceExtensionView.name(), TestKonvenceExtension.class);
        testKonvenceExtension.getEnabled().convention(true);
        var reverseTransformationSpec = objects.newInstance(ReverseTransformationSpec.class);
        reverseTransformationSpec.getEnabled().convention(true);
        testKonvenceExtension.getReverseTransformation().convention(reverseTransformationSpec);
        testKonvenceExtension.getApplyAutomaticallyAfterTestTask().convention(true);
        testKonvenceExtension.getCamelCaseMethodNameProperty().convention(false);

        project.afterEvaluate(p -> {
            project.getPlugins().withId("java", javaPlugin -> {
                var testKonvenceEnforceAllTask =
                        tasks.register(testKonvenceEnforceAllTaskName, testKonvenceEnforceAll -> {
                            testKonvenceEnforceAll.getOutputs().upToDateWhen(ignore -> false);
                            testKonvenceEnforceAll.setGroup(testKonvenceTaskGroup);
                            testKonvenceEnforceAll.dependsOn(tasks.withType(JavaCompile.class));
                        });
                var testKonvenceDryEnforceWithFailingTask =
                        tasks.register(testKonvenceDryEnforceWithFailingTaskName, testKonvenceEnforceAll -> {
                            testKonvenceEnforceAll.getOutputs().upToDateWhen(ignore -> false);
                            testKonvenceEnforceAll.setGroup(testKonvenceTaskGroup);
                            testKonvenceEnforceAll.dependsOn(tasks.withType(JavaCompile.class));
                        });

                var log = project.getLogger();
                var layout = project.getLayout();
                var buildDirectory = layout.getBuildDirectory();
                var testing = (TestingExtension) extensions.getByName("testing");
                var annotationProcessorTargetPathProvider = buildDirectory.map(dir -> dir.getAsFile()
                        .toPath()
                        .resolve("tmp")
                        .resolve("testkonvenceplugin")
                        .resolve(Constants.PROCESSOR_JAR));
                var setupAnnotationProcessorTaskProvider =
                        tasks.register("setupTestKonvenceAnnotationProcessor", task -> {
                            task.doLast(new ConfigureOnBeforeCompileTestStart(annotationProcessorTargetPathProvider));
                            task.getOutputs().file(annotationProcessorTargetPathProvider);
                        });

                testing.getSuites().configureEach(testSuite -> {
                    if (testSuite instanceof JvmTestSuite) {
                        var jvmTestSuite = (JvmTestSuite) testSuite;
                        var testSourceSet = jvmTestSuite.getSources();
                        // configure compile task to use annotation processor
                        tasks.named(testSourceSet.getCompileJavaTaskName(), JavaCompile.class)
                                .configure(compileTestJava -> {
                                    compileTestJava.dependsOn(setupAnnotationProcessorTaskProvider);
                                    compileTestJava.mustRunAfter(setupAnnotationProcessorTaskProvider);
                                    var options = compileTestJava.getOptions();
                                    var args = new ArrayList<>(options.getCompilerArgs());
                                    args.add("-A" + Constants.apUseCamelCaseMethodNamesOption + "="
                                            + testKonvenceExtension
                                                    .getCamelCaseMethodNameProperty()
                                                    .get());
                                    args.add("-A" + Constants.apReversedOption + "="
                                            + reverseTransformationSpec
                                                    .getEnabled()
                                                    .get());
                                    options.setCompilerArgs(args);
                                });
                        var name = testSuite.getName();
                        var processorJar = buildDirectory.files("tmp/testkonvenceplugin/" + Constants.PROCESSOR_JAR);
                        dependencies.add(name + "AnnotationProcessor", processorJar);

                        jvmTestSuite.getTargets().configureEach(testTarget -> {
                            var testTask = testTarget.getTestTask();
                            log.debug("Configuring test task [{}]", testTask.getName());
                            var enforceFilesCollection = objects.fileCollection();
                            enforceFilesCollection.setFrom(
                                    buildDirectory.map(
                                            buildDir -> buildDir.getAsFileTree()
                                                    .matching(
                                                            pattern -> pattern.include(
                                                                    "generated/sources/annotationProcessor/**/testkonvence_enforcements.xml"))));
                            var compileClasspath = objects.fileCollection();
                            compileClasspath.setFrom(testSourceSet.getCompileClasspath());
                            var testNameEnforceAction = new TestNameEnforceAction(
                                    objects.fileCollection().from(project.provider(() -> testSourceSet
                                            .getJava()
                                            .getSrcDirs()
                                            .iterator()
                                            .next())),
                                    compileClasspath,
                                    enforceFilesCollection,
                                    testKonvenceExtension.getEnabled(),
                                    testKonvenceExtension.getApplyAutomaticallyAfterTestTask(),
                                    testKonvenceExtension.getCamelCaseMethodNameProperty(),
                                    testKonvenceExtension
                                            .getReverseTransformation()
                                            .get()
                                            .getEnabled());
                            var enforceTaskProvider = tasks.register(
                                    TestNameEnforceTask.name(testTask.getName()),
                                    TestNameEnforceTask.class,
                                    enforceTask -> {
                                        enforceTask.getDryWithFailing().convention(false);
                                        enforceTask.getUseCamelCaseMethodName().convention(false);
                                        enforceTask
                                                .getEnableReverseTransformation()
                                                .convention(false);

                                        enforceTask.getPluginEnabled().convention(testKonvenceExtension.getEnabled());
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

                            testKonvenceEnforceAllTask.configure(task -> task.doLast(newEnforceAction(
                                    testKonvenceExtension, testNameEnforceAction, providers.provider(() -> false))));
                            testKonvenceDryEnforceWithFailingTask.configure(task -> task.doLast(newEnforceAction(
                                    testKonvenceExtension, testNameEnforceAction, providers.provider(() -> true))));
                        });
                    }
                });
            });
        });
    }

    private TestNameEnforceAction newEnforceAction(
            TestKonvenceExtension extension,
            TestNameEnforceAction testNameEnforceAction,
            Provider<Boolean> dryWithFailingProvider) {
        return new TestNameEnforceAction(
                testNameEnforceAction.sourcesRootProp(),
                testNameEnforceAction.compileClasspath(),
                testNameEnforceAction.enforceFiles(),
                extension.getEnabled(),
                dryWithFailingProvider,
                testNameEnforceAction.camelCaseMethodNameProvider(),
                testNameEnforceAction.enableReverseTransformation());
    }
}
