package com.varlanv.testkonvence.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import com.varlanv.testkonvence.gradle.plugin.internal.ApplyInternalPluginLogic;
import com.varlanv.testkonvence.gradle.plugin.internal.InternalEnvironment;
import com.varlanv.testkonvence.gradle.plugin.internal.InternalProperties;

import java.util.Objects;

public class InternalConventionPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        var extensions = project.getExtensions();
        var providers = project.getProviders();
        var environment = (InternalEnvironment) Objects.requireNonNullElseGet(
            extensions.findByName(InternalEnvironment.name()),
            () -> new InternalEnvironment(
                providers.environmentVariable("CI").isPresent(),
                false
            )
        );
        var properties = (InternalProperties) Objects.requireNonNullElseGet(
            extensions.findByName(InternalProperties.name()),
            () -> new InternalProperties(((VersionCatalogsExtension) extensions.getByName("versionCatalogs")).named("libs")));
        var internalConventionExtension = (InternalConventionExtension) Objects.requireNonNullElseGet(
            extensions.findByName(InternalConventionExtension.name()),
            () -> extensions.create(InternalConventionExtension.name(), InternalConventionExtension.class));
        internalConventionExtension.getIntegrationTestName().convention("integrationTest");

        new ApplyInternalPluginLogic(
            project.getPath(),
            project.getPluginManager(),
            project.getRepositories(),
            project.getDependencies(),
            extensions,
            internalConventionExtension,
            project.getComponents(),
            project.getTasks(),
            project.getConfigurations(),
            environment,
            properties,
            project.getName(),
            project.getGradle().getSharedServices(),
            project.getLayout(),
            project.getRootDir(),
            runnable -> project.afterEvaluate(ignore -> runnable.run())
        ).apply();
    }
}
