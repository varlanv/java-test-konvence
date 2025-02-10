package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.gradle.plugin.internal.InternalEnvironment;
import com.varlanv.testkonvence.gradle.plugin.internal.InternalProperties;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.plugins.quality.*;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.VariantVersionMappingStrategy;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JvmVendorSpec;
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.gradle.testing.base.TestingExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InternalConventionPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        var extensions = project.getExtensions();
        var providers = project.getProviders();
        var projectPath = project.getPath();
        var pluginManager = project.getPluginManager();
        var repositories = project.getRepositories();
        var dependencies = project.getDependencies();
        var components = project.getComponents();
        var tasks = project.getTasks();
        var configurations = project.getConfigurations();
        var projectName = project.getName();
        var projectLayout = project.getLayout();
        var rootDir = project.getRootDir();

        var internalEnvironment = (InternalEnvironment) Objects.requireNonNullElseGet(
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

        var isGradlePlugin = projectName.endsWith("plugin");

        var targetJavaVersion = 17;

        if (isGradlePlugin) {
            pluginManager.apply(JavaGradlePluginPlugin.class);
        }

        // ---------- Configure repositories start ----------
        if (internalEnvironment.isLocal()) {
            repositories.add(repositories.mavenLocal());
        }
        repositories.add(repositories.mavenCentral());
        // ---------- Configure repositories end ----------

        // ---------- Apply common java plugins start ----------
        pluginManager.withPlugin(
            "java",
            ignore -> {
                pluginManager.apply(PmdPlugin.class);
                pluginManager.apply(CheckstylePlugin.class);
                if (internalEnvironment.isLocal()) {
                    pluginManager.apply(IdeaPlugin.class);
                    var idea = (IdeaModel) extensions.getByName("idea");
                    idea.getModule().setDownloadJavadoc(true);
                    idea.getModule().setDownloadSources(true);
                }
            }
        );
        // ---------- Apply common java plugins end ----------

        // ---------- Configure Java start ----------
        pluginManager.withPlugin(
            "java",
            plugin -> {
                var java = (JavaPluginExtension) extensions.getByName("java");
                java.withSourcesJar();
                if (internalEnvironment.isCi()) {
                    java.setSourceCompatibility(JavaLanguageVersion.of(targetJavaVersion));
                    java.setTargetCompatibility(JavaLanguageVersion.of(targetJavaVersion));
                } else {
                    java.toolchain(
                        toolchain -> {
                            toolchain.getVendor().set(JvmVendorSpec.AZUL);
                            toolchain.getLanguageVersion().set(JavaLanguageVersion.of(targetJavaVersion));
                        }
                    );
                }
            }
        );
        // ---------- Configure Java end ----------

        // ---------- Add common dependencies start ----------
        pluginManager.withPlugin(
            "java",
            plugin -> {
                var jetbrainsAnnotations = properties.getLib("jetbrains-annotations");
                dependencies.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, jetbrainsAnnotations);
                dependencies.add(JavaPlugin.TEST_COMPILE_ONLY_CONFIGURATION_NAME, jetbrainsAnnotations);
                dependencies.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("assertj-core"));
                dependencies.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("junit-jupiter-api"));
                dependencies.add(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME, properties.getLib("junit-platform-launcher"));

                var lombokDependency = properties.getLib("lombok");
                dependencies.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, lombokDependency);
                dependencies.add(JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME, lombokDependency);
                dependencies.add(JavaPlugin.TEST_COMPILE_ONLY_CONFIGURATION_NAME, lombokDependency);
                dependencies.add(JavaPlugin.TEST_ANNOTATION_PROCESSOR_CONFIGURATION_NAME, lombokDependency);

                if (!internalEnvironment.isTest() && !projectPath.equals(":common-test")) {
                    dependencies.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, dependencies.project(Map.of("path", ":common-test")));
                }
            }
        );
        // ---------- Add common dependencies end ----------

        var javaToolchainService = extensions.getByType(JavaToolchainService.class);

        project.afterEvaluate(ignore -> {
                // ---------- Configure tests start ----------
                // need to configure these things after project evaluate so that extension is evaluated
                var integrationTestTaskName = internalConventionExtension.getIntegrationTestName().get();
                pluginManager.withPlugin(
                    "java",
                    plugin -> {
                        var testing = (TestingExtension) extensions.getByName("testing");
                        var suites = testing.getSuites();
                        var integrationTestSuite = suites.register(
                            integrationTestTaskName,
                            JvmTestSuite.class,
                            suite -> suite.getTargets().all(
                                target -> target.getTestTask().configure(
                                    test -> test.getJavaLauncher().set(javaToolchainService.launcherFor(
                                            config -> {
                                                config.getLanguageVersion().set(JavaLanguageVersion.of(17));
                                                config.getVendor().set(JvmVendorSpec.AZUL);
                                            }
                                        )
                                    )
                                )
                            )
                        );
                        suites.configureEach(
                            suite -> {
                                if (suite instanceof JvmTestSuite) {
                                    var jvmTestSuite = (JvmTestSuite) suite;
                                    jvmTestSuite.useJUnitJupiter();
                                    jvmTestSuite.dependencies(
                                        jvmComponentDependencies -> {
                                            var implementation = jvmComponentDependencies.getImplementation();
                                            implementation.add(jvmComponentDependencies.project());
                                        }
                                    );
                                    jvmTestSuite.getTargets().all(
                                        target -> target.getTestTask().configure(
                                            test -> {
                                                // remove next line to allow tests caching
                                                test.getOutputs().upToDateWhen(task -> false);
                                                test.testLogging(
                                                    logging -> {
                                                        logging.setShowStandardStreams(true);
                                                        logging.setShowStackTraces(true);
                                                    }
                                                );
                                                test.setFailFast(internalEnvironment.isCi());
                                                var memory = "1024m";
                                                test.setJvmArgs(
                                                    Stream.of(
                                                            test.getJvmArgs(),
                                                            List.of("-Xms" + memory, "-Xmx" + memory),
                                                            List.of("-XX:TieredStopAtLevel=1", "-noverify")
                                                        )
                                                        .flatMap(Collection::stream)
                                                        .collect(Collectors.toList())
                                                );
                                            }
                                        )
                                    );
                                }
                            }
                        );
                        tasks.named("check", task -> task.dependsOn(integrationTestSuite));

                        // configure integration test configurations to extend from base test configuration
                        configurations.named(
                            integrationTestTaskName + "Implementation",
                            configuration -> configuration.extendsFrom(configurations.getByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME))
                        );
                        configurations.named(
                            integrationTestTaskName + "AnnotationProcessor",
                            configuration -> configuration.extendsFrom(configurations.getByName(JavaPlugin.TEST_ANNOTATION_PROCESSOR_CONFIGURATION_NAME))
                        );
                        configurations.named(
                            integrationTestTaskName + "CompileOnly",
                            configuration -> configuration.extendsFrom(configurations.getByName(JavaPlugin.TEST_COMPILE_ONLY_CONFIGURATION_NAME))
                        );
                        configurations.named(
                            integrationTestTaskName + "RuntimeOnly",
                            configuration -> configuration.extendsFrom(configurations.getByName(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME))
                        );
                    }
                );
                // ---------- Configure tests end ----------

                // ---------- Configure publishing start ----------
                if (!isGradlePlugin) {
                    pluginManager.withPlugin(
                        "maven-publish",
                        plugin -> extensions.configure(
                            PublishingExtension.class,
                            publishingExtension -> publishingExtension.getPublications().create(
                                "mavenJava",
                                MavenPublication.class,
                                mavenPublication -> {
                                    SoftwareComponent javaComponent = components.getByName("java");
                                    mavenPublication.from(javaComponent);
                                    mavenPublication.versionMapping(
                                        versionMappingStrategy -> {
                                            versionMappingStrategy.usage(
                                                "java-api",
                                                variantVersionMappingStrategy ->
                                                    variantVersionMappingStrategy.fromResolutionOf(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME));
                                            versionMappingStrategy.usage("java-runtime", VariantVersionMappingStrategy::fromResolutionResult);
                                        }
                                    );
                                }
                            )
                        )
                    );
                }
                // ---------- Configure publishing end ----------

                // ---------- Configure static analysis start ----------
                var staticAnalyseFolder = rootDir.toPath().resolve(".static-analyse");

                // Configure aggregate static analysis tasks
                var staticAnalyseMain = tasks.register(
                    "staticAnalyseMain",
                    task -> {
                        task.setGroup("static analysis");
                        task.setDescription("Run static analysis on main sources");
                    }
                );
                var staticAnalyseTest = tasks.register(
                    "staticAnalyseTest",
                    task -> {
                        task.setGroup("static analysis");
                        task.setDescription("Run static analysis on test sources");
                    }
                );
                tasks.register(
                    "staticAnalyseFull",
                    task -> {
                        task.setGroup("static analysis");
                        task.setDescription("Run static analysis on all sources");
                        task.dependsOn(staticAnalyseMain, staticAnalyseTest);
                    }
                );

                tasks.named(
                    "check",
                    task -> {
                        task.dependsOn(staticAnalyseMain);
                        task.dependsOn(staticAnalyseTest);
                    }
                );

                // Configure pmd
                pluginManager.withPlugin(
                    "pmd",
                    pmd -> {
                        var pmdExtension = (PmdExtension) extensions.getByName("pmd");
                        pmdExtension.setRuleSetFiles(
                            projectLayout.files(
                                staticAnalyseFolder.resolve("pmd.xml")
                            )
                        );
                        pmdExtension.setToolVersion(properties.getVersion("pmdTool"));
                        var pmdMainTask = tasks.named(
                            "pmdMain",
                            Pmd.class,
                            pmdTask -> pmdTask.setRuleSetFiles(
                                projectLayout.files(
                                    staticAnalyseFolder.resolve("pmd.xml")
                                )
                            )
                        );
                        var pmdTestTasks = Stream.of(JavaPlugin.TEST_TASK_NAME, internalConventionExtension.getIntegrationTestName().get())
                            .map(testTaskName -> "pmd" + capitalize(testTaskName))
                            .map(
                                taskName -> tasks.named(
                                    taskName,
                                    Pmd.class,
                                    pmdTask -> pmdTask.setRuleSetFiles(
                                        projectLayout.files(
                                            staticAnalyseFolder.resolve("pmd-test.xml")
                                        )
                                    )
                                )
                            )
                            .collect(Collectors.toList());
                        staticAnalyseMain.configure(task -> task.dependsOn(pmdMainTask));
                        staticAnalyseTest.configure(task -> task.dependsOn(pmdTestTasks));
                    }
                );

                // Configure checkstyle
                pluginManager.withPlugin(
                    "checkstyle",
                    checkstyle -> {
                        var checkstyleExtension = extensions.getByType(CheckstyleExtension.class);
                        checkstyleExtension.setToolVersion(properties.getVersion("checkstyleTool"));
                        checkstyleExtension.setMaxWarnings(0);
                        checkstyleExtension.setMaxErrors(0);
                        checkstyleExtension.setConfigFile(staticAnalyseFolder.resolve("checkstyle.xml").toFile());

                        var checkstyleMainTask = tasks.named("checkstyleMain");
                        var checkstyleTestTasks = Stream.of("test", internalConventionExtension.getIntegrationTestName().get())
                            .map(string -> "checkstyle" + string.substring(0, 1).toUpperCase() + string.substring(1))
                            .map(taskName -> tasks.named(taskName, Task.class))
                            .collect(Collectors.toList());

                        staticAnalyseMain.configure(task -> task.dependsOn(checkstyleMainTask));
                        staticAnalyseTest.configure(task -> task.dependsOn(checkstyleTestTasks));
                    }
                );
                // ---------- Configure static analysis end ----------
            }
        );
    }

    private String capitalize(String string) {
        var chars = string.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
}
