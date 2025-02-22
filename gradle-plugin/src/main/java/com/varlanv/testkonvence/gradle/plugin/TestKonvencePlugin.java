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
            val log = project.getLogger();
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
            testing.getSuites().configureEach(suite -> {
                if (suite instanceof JvmTestSuite) {
                    val jvmSuite = (JvmTestSuite) suite;
                    jvmSuite.sources(testSourceSet -> {
                        val compileTestJava = (JavaCompile) tasks.findByName(testSourceSet.getCompileJavaTaskName());
                        if (compileTestJava == null) {
                            log.error("Did not find compile java task for test source set [{}]", testSourceSet.getName());
                            return;
                        }
                        compileTestJava.dependsOn(setupAnnotationProcessorTaskProvider);
                        compileTestJava.mustRunAfter(setupAnnotationProcessorTaskProvider);
                        val enforceFilesCollection = objects.fileCollection();
                        enforceFilesCollection.setFrom(
                            buildDirectory.map(buildDir -> buildDir
                                .getAsFileTree()
                                .matching(pattern -> pattern.include("generated/sources/annotationProcessor/**/testkonvence_enforcements.xml"))
                            )
                        );
                        val options = compileTestJava.getOptions();
                        val processorJar = buildDirectory.files("tmp/testkonvenceplugin/" + Constants.PROCESSOR_JAR);
                        val annotationProcessorClasspath = Optional.ofNullable(options.getAnnotationProcessorPath())
                            .map(f -> f.plus(processorJar))
                            .orElse(processorJar);
                        options.setAnnotationProcessorPath(annotationProcessorClasspath);
                        val sourcesRootProp = objects.fileCollection().from(
                            project.provider(
                                () -> testSourceSet.getJava().getSrcDirs().iterator().next()
                            )
                        );
                        val compileClasspathCollection = objects.fileCollection();
                        compileClasspathCollection.setFrom(testSourceSet.getCompileClasspath());
                        jvmSuite.getTargets().configureEach(target -> {
                            val enforceTaskProvider = tasks.register(testSourceSet.getName() + "KonvenceEnforce", enforceTask -> {
                                enforceTask.doLast(
                                    new TestNameEnforceAction(
                                        sourcesRootProp,
                                        compileClasspathCollection,
                                        enforceFilesCollection
                                    )
                                );
                                val inputs = enforceTask.getInputs();
                                inputs.files(sourcesRootProp);
                                inputs.files(compileClasspathCollection);
                                inputs.files(enforceFilesCollection);
                                // todo configure output
//                                val outputs = enforceTask.getOutputs();
                            });
                            target.getTestTask().configure(test -> {
                                test.finalizedBy(enforceTaskProvider);
                            });
                        });
                    });
                }
            });
        });
    }
}
