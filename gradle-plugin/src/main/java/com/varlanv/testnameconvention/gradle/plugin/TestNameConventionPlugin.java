package com.varlanv.testnameconvention.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;

import java.util.Map;

public class TestNameConventionPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.afterEvaluate(ignore -> {
                    project.getLogger().lifecycle("Hello World!");
                    project.getPlugins().withId("java", javaPlugin -> {
                        var dependencies = project.getDependencies();
//                        dependencies.add(JavaPlugin.TEST_ANNOTATION_PROCESSOR_CONFIGURATION_NAME, dependencies.project(Map.of("path", ":annotation-processor")));
                        var tasks = project.getTasks();
                        var enforceTaskProvider = tasks.register(
                                TestNameEnforceTask.name(),
                                TestNameEnforceTask.class,
                                enforceTask -> {
                                    var path = "build/generated/sources/annotationProcessor/java/integrationTest/com/varlanv/testnameconvention/enforcements.xml";
                                    enforceTask.getInputFile().convention(
                                            project.getLayout().file(
                                                    project.provider(
                                                            () -> project.file(path)
                                                    )
                                            )
                                    );
                                });
                        tasks.withType(Test.class, test -> {
//                            test.dependsOn(enforceTaskProvider);
                            var testCompileTask = test.getName() + "Classes";
//                            enforceTaskProvider.configure(enforceTask -> {
//                                enforceTask.dependsOn(testCompileTask);
//                                enforceTask.mustRunAfter(testCompileTask);
//                            });
                        });
                    });
                }
        );
    }
}
