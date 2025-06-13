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

    @Override
    public void apply(Project project) {
        var extensions = project.getExtensions();
        var dependencies = project.getDependencies();
        var objects = project.getObjects();
        var tasks = project.getTasks();
        var providers = project.getProviders();
        var performanceLogEnabled = providers
                .gradleProperty(Constants.performanceLogProperty)
                .map(Boolean::parseBoolean)
                .orElse(false);
        var isCi = providers.gradleProperty("CI").isPresent();
        var testKonvenceExtension = (TestKonvenceExtension) extensions.create(
                TestKonvenceExtensionView.class, TestKonvenceExtensionView.name(), TestKonvenceExtension.class);
        testKonvenceExtension.getEnabled().convention(true);
        var reverseTransformationSpec = objects.newInstance(ReverseTransformationSpec.class);
        reverseTransformationSpec.getEnabled().convention(true);
        testKonvenceExtension.getReverseTransformation().convention(reverseTransformationSpec);
        testKonvenceExtension.getApplyAutomaticallyAfterTestTask().convention(!isCi);
        testKonvenceExtension.getCamelCaseMethodNameProperty().convention(false);

        project.afterEvaluate(p -> {
            project.getPlugins().withId("java", javaPlugin -> {
                var testKonvenceApplyTask =
                        tasks.register(Constants.TEST_KONVENCE_APPLY_TASK_NAME, testKonvenceApply -> {
                            testKonvenceApply.getOutputs().upToDateWhen(ignore -> false);
                            testKonvenceApply.setGroup(Constants.TEST_KONVENCE_TASK_GROUP);
                            testKonvenceApply.dependsOn(tasks.withType(JavaCompile.class));
                        });
                var testKonvenceVerifyTask =
                        tasks.register(Constants.TEST_KONVENCE_VERIFY_TASK_NAME, testKonvenceVerify -> {
                            testKonvenceVerify.getOutputs().upToDateWhen(ignore -> false);
                            testKonvenceVerify.setGroup(Constants.TEST_KONVENCE_TASK_GROUP);
                            testKonvenceVerify.dependsOn(tasks.withType(JavaCompile.class));
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
                        var testCompileTaskProvider =
                                tasks.named(testSourceSet.getCompileJavaTaskName(), JavaCompile.class);
                        testCompileTaskProvider.configure(compileTestJava -> {
                            compileTestJava.dependsOn(setupAnnotationProcessorTaskProvider);
                            compileTestJava.mustRunAfter(setupAnnotationProcessorTaskProvider);
                            var options = compileTestJava.getOptions();
                            var args = new ArrayList<>(options.getCompilerArgs());
                            args.add("-A" + Constants.apUseCamelCaseMethodNamesOption + "="
                                    + testKonvenceExtension
                                            .getCamelCaseMethodNameProperty()
                                            .get());
                            args.add("-A" + Constants.apReversedOption + "="
                                    + reverseTransformationSpec.getEnabled().get());
                            args.add("-A" + Constants.performanceLogProperty + "=" + performanceLogEnabled.get());
                            options.setCompilerArgs(args);
                        });
                        var name = testSuite.getName();
                        var processorJar = buildDirectory.files("tmp/testkonvenceplugin/" + Constants.PROCESSOR_JAR);
                        dependencies.add(name + "AnnotationProcessor", processorJar);

                        jvmTestSuite.getTargets().configureEach(testTarget -> {
                            var testTask = testTarget.getTestTask();
                            log.debug("Configuring test task [{}]", testTask.getName());
                            var enforceFilesCollection = objects.fileCollection();
                            enforceFilesCollection.setFrom(buildDirectory.map(buildDir -> buildDir.getAsFileTree()
                                    .matching(pattern -> pattern.include(String.format(
                                            "generated/sources/annotationProcessor/java/%s/%s/%s",
                                            testSourceSet.getName(),
                                            Constants.apEnforcementsXmlPackage.replace('.', '/'),
                                            Constants.apEnforcementsXmlName)))));
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
                                            .getEnabled(),
                                    performanceLogEnabled);
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
                                        enforceTask.getPerformanceLogEnabled().set(performanceLogEnabled);
                                    });
                            if (testKonvenceExtension
                                    .getApplyAutomaticallyAfterTestTask()
                                    .get()) {
                                testTask.configure(test -> {
                                    test.finalizedBy(enforceTaskProvider);
                                });
                            }

                            testKonvenceApplyTask.configure(task -> task.doLast(newEnforceAction(
                                    testKonvenceExtension, testNameEnforceAction, providers.provider(() -> false))));
                            testKonvenceVerifyTask.configure(task -> task.doLast(newEnforceAction(
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
                testNameEnforceAction.enableReverseTransformation(),
                testNameEnforceAction.performanceLogEnabled());
    }
}
