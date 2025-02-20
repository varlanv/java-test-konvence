package com.varlanv.testkonvence.gradle.plugin;

import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.testing.Test;

public class TestKonvencePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().withId("java", javaPlugin -> {
            project.getLogger().lifecycle("Hello World!");
            val dependencies = project.getDependencies();
            val extensions = project.getExtensions();
            val javaExtension = extensions.getByType(JavaPluginExtension.class);
            val javaSourceSets = javaExtension.getSourceSets();
            val mainSourceSet = javaSourceSets.getByName("main");
            val compileClasspath = mainSourceSet.getCompileClasspath();
            val tasks = project.getTasks();
            val enforceTaskProvider = tasks.register(
                TestNameEnforceTask.name(),
                TestNameEnforceTask.class,
                enforceTask -> {
                    enforceTask.getSourcesRoot().fileProvider(project.provider(() -> mainSourceSet.getJava().getSrcDirs().iterator().next()));
                    enforceTask.getCompileClasspath().from(compileClasspath);
                    enforceTask.getEnforceFiles().setFrom(
                        project.getLayout()
                            .getBuildDirectory()
                            .map(buildDir -> buildDir
                                .getAsFileTree()
                                .matching(pattern -> pattern.include("generated/sources/annotationProcessor/**/testkonvence_enforcements.xml")))
                    );
                    enforceTask.mustRunAfter(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME);
                });
            tasks.withType(Test.class).configureEach(test -> {
                test.dependsOn(enforceTaskProvider);
            });
        });
    }
}
