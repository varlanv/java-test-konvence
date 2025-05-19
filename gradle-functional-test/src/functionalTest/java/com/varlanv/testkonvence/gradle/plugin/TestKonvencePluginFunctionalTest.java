package com.varlanv.testkonvence.gradle.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import com.varlanv.testkonvence.commontest.*;
import com.varlanv.testkonvence.commontest.sample.ImmutableSampleOptions;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Execution(ExecutionMode.SAME_THREAD)
@Tags({@Tag(BaseTest.FUNCTIONAL_TEST_TAG), @Tag(BaseTest.SLOW_TEST_TAG)})
class TestKonvencePluginFunctionalTest implements FunctionalTest {

    String defaultSettingsGradleConfig = groovy("""
        rootProject.name = "functional-test"
        """);

    @TestFactory
    Stream<DynamicTest> test_gradle_version_compatibility() {
        return TestSamples.testSamples().stream()
                .limit(1)
                .flatMap(sample -> TestGradleVersions.list().stream().map(version -> Map.entry(sample, version)))
                .map(sampleEntry -> DynamicTest.dynamicTest(
                        "GRADLE VERSION [%s] %s"
                                .formatted(
                                        sampleEntry.getValue(),
                                        sampleEntry.getKey().description()),
                        () -> {
                            sampleEntry.getKey().consume(consumableSample -> {
                                runGradleRunnerFixture(
                                        new DataTable(false, false, false, sampleEntry.getValue()),
                                        List.of("test"),
                                        (fixture) -> {
                                            Files.writeString(fixture.settingsFile(), defaultSettingsGradleConfig);

                                            Files.writeString(
                                                    fixture.rootBuildFile(),
                                                    defaultBuildGradleConfig(options -> options.camelMethodName(
                                                                    consumableSample
                                                                            .options()
                                                                            .camelMethodName())
                                                            .reverseTransformation(
                                                                    consumableSample
                                                                            .options()
                                                                            .reverseTransformation())));
                                            var javaDir = Files.createDirectories(fixture.subjectProjectDir()
                                                    .resolve("src")
                                                    .resolve("test")
                                                    .resolve("java"));
                                            for (var sampleSourceFile : consumableSample.sources()) {
                                                var relativeSourceFilePath =
                                                        consumableSample.dir().relativize(sampleSourceFile.path());
                                                var sourceFile = javaDir.resolve(relativeSourceFilePath);
                                                Files.createDirectories(sourceFile.getParent());
                                                Files.move(sampleSourceFile.path(), sourceFile);
                                            }
                                            build(fixture.runner(), GradleRunner::build);
                                            for (var sampleSourceFile : consumableSample.sources()) {
                                                var relativeSourceFilePath =
                                                        consumableSample.dir().relativize(sampleSourceFile.path());
                                                var sourceFile = javaDir.resolve(relativeSourceFilePath);
                                                var modifiedSourceFileContent = Files.readString(sourceFile);
                                                assertThat(modifiedSourceFileContent)
                                                        .isEqualTo(sampleSourceFile.expectedTransformation());
                                            }
                                        });
                            });
                        }));
    }

    @TestFactory
    Stream<DynamicTest> fromSamples() {
        return TestSamples.testSamples().stream()
                .map(sample -> DynamicTest.dynamicTest(
                        sample.description(),
                        () -> sample.consume(consumableSample -> runGradleRunnerFixture(
                                new DataTable(false, false, false, TestGradleVersions.current()),
                                List.of("test"),
                                (fixture) -> {
                                    Files.writeString(fixture.settingsFile(), defaultSettingsGradleConfig);

                                    Files.writeString(
                                            fixture.rootBuildFile(),
                                            defaultBuildGradleConfig(options -> options.camelMethodName(consumableSample
                                                            .options()
                                                            .camelMethodName())
                                                    .reverseTransformation(consumableSample
                                                            .options()
                                                            .reverseTransformation())));
                                    var javaDir = Files.createDirectories(fixture.subjectProjectDir()
                                            .resolve("src")
                                            .resolve("test")
                                            .resolve("java"));
                                    for (var sampleSourceFile : consumableSample.sources()) {
                                        var relativeSourceFilePath =
                                                consumableSample.dir().relativize(sampleSourceFile.path());
                                        var sourceFile = javaDir.resolve(relativeSourceFilePath);
                                        Files.createDirectories(sourceFile.getParent());
                                        Files.move(sampleSourceFile.path(), sourceFile);
                                    }
                                    build(fixture.runner(), GradleRunner::build);
                                    for (var sampleSourceFile : consumableSample.sources()) {
                                        var relativeSourceFilePath =
                                                consumableSample.dir().relativize(sampleSourceFile.path());
                                        var sourceFile = javaDir.resolve(relativeSourceFilePath);
                                        var modifiedSourceFileContent = Files.readString(sourceFile);
                                        assertThat(modifiedSourceFileContent)
                                                .isEqualTo(sampleSourceFile.expectedTransformation());
                                    }
                                }))));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("If requesting dry enforce with failing and there is enforcement fail, then should fail build")
    void should_fail_when_dry_enforce_with_failing_and_there_is_enforcement_fail_then_fail_build(
            Boolean applyAutomaticallyAfterTestTask) {
        var sample = TestSamples.testSamples().stream()
                .filter(s -> Objects.equals(s.description(), "Should replace method name if found"))
                .findFirst()
                .orElseThrow();
        sample.consume(consumableSample -> {
            runGradleRunnerFixture(
                    new DataTable(false, false, false, TestGradleVersions.current()),
                    List.of("testKonvenceDryEnforceWithFailing"),
                    (fixture) -> {
                        Files.writeString(fixture.settingsFile(), defaultSettingsGradleConfig);

                        Files.writeString(
                                fixture.rootBuildFile(),
                                defaultBuildGradleConfig(options ->
                                        options.applyAutomaticallyAfterTestTask(applyAutomaticallyAfterTestTask)));
                        var javaDir = Files.createDirectories(fixture.subjectProjectDir()
                                .resolve("src")
                                .resolve("test")
                                .resolve("java"));
                        var sampleSourceFile = consumableSample.sourceFile();
                        var contentBefore = sampleSourceFile.content();
                        var relativeSourceFilePath = consumableSample.dir().relativize(sampleSourceFile.path());
                        var sourceFile = javaDir.resolve(relativeSourceFilePath);
                        Files.createDirectories(sourceFile.getParent());
                        Files.move(sampleSourceFile.path(), sourceFile);

                        var buildResult = build(fixture.runner(), GradleRunner::buildAndFail);
                        assertThat(buildResult.getOutput()).contains("found test naming mismatch in file");

                        var newSourceFileContent = Files.readString(sourceFile);
                        assertThat(newSourceFileContent).isEqualTo(contentBefore);
                    });
        });
    }

    @Test
    @DisplayName(
            "If set property 'applyAutomaticallyAfterTestTask' to false, then should not apply enforce after test task")
    void should_not_apply_enforce_when_applyAutomaticallyAfterTestTask_set_to_false() {
        var sample = TestSamples.testSamples().stream()
                .filter(s -> Objects.equals(s.description(), "Should replace method name if found"))
                .findFirst()
                .orElseThrow();
        sample.consume(consumableSample -> {
            runGradleRunnerFixture(
                    new DataTable(false, false, false, TestGradleVersions.current()), List.of("test"), (fixture) -> {
                        Files.writeString(fixture.settingsFile(), defaultSettingsGradleConfig);

                        Files.writeString(
                                fixture.rootBuildFile(),
                                defaultBuildGradleConfig(options -> options.applyAutomaticallyAfterTestTask(false)));
                        var javaDir = Files.createDirectories(fixture.subjectProjectDir()
                                .resolve("src")
                                .resolve("test")
                                .resolve("java"));
                        var sampleSourceFile = consumableSample.sourceFile();
                        var contentBefore = sampleSourceFile.content();
                        var relativeSourceFilePath = consumableSample.dir().relativize(sampleSourceFile.path());
                        var sourceFile = javaDir.resolve(relativeSourceFilePath);
                        Files.createDirectories(sourceFile.getParent());
                        Files.move(sampleSourceFile.path(), sourceFile);

                        build(fixture.runner(), GradleRunner::build);

                        var newSourceFileContent = Files.readString(sourceFile);
                        assertThat(newSourceFileContent).isEqualTo(contentBefore);
                    });
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("'testKonvenceEnforceAll' task should replace method names if found")
    void testKonvenceEnforceAll_should_replace_method_names_if_found(Boolean applyAutomaticallyAfterTestTask) {
        var sample = TestSamples.testSamples().stream()
                .filter(s -> Objects.equals(s.description(), "Should replace method name if found"))
                .findFirst()
                .orElseThrow();
        sample.consume(consumableSample -> {
            runGradleRunnerFixture(
                    new DataTable(false, false, false, TestGradleVersions.current()),
                    List.of("testKonvenceEnforceAll"),
                    (fixture) -> {
                        Files.writeString(fixture.settingsFile(), defaultSettingsGradleConfig);

                        Files.writeString(
                                fixture.rootBuildFile(),
                                defaultBuildGradleConfig(options ->
                                        options.applyAutomaticallyAfterTestTask(applyAutomaticallyAfterTestTask)));
                        var javaDir = Files.createDirectories(fixture.subjectProjectDir()
                                .resolve("src")
                                .resolve("test")
                                .resolve("java"));
                        var sampleSourceFile = consumableSample.sourceFile();
                        var relativeSourceFilePath = consumableSample.dir().relativize(sampleSourceFile.path());
                        var sourceFile = javaDir.resolve(relativeSourceFilePath);
                        Files.createDirectories(sourceFile.getParent());
                        Files.move(sampleSourceFile.path(), sourceFile);

                        build(fixture.runner(), GradleRunner::build);

                        var newSourceFileContent = Files.readString(sourceFile);
                        assertThat(newSourceFileContent).isEqualTo(sampleSourceFile.expectedTransformation());
                    });
        });
    }

    private String defaultBuildGradleConfig(
            Function<ImmutableSampleOptions.Builder, ImmutableSampleOptions.Builder> configure) {
        var options = configure.apply(ImmutableSampleOptions.builder()).build();
        return groovy(
                """
            plugins {
                id("java")
                id("com.varlanv.test-konvence")
            }

            repositories {
                mavenLocal()
                mavenCentral()
            }

            testKonvence {
                applyAutomaticallyAfterTestTask(%s)
                useCamelCaseForMethodNames(%s)
                reverseTransformation {
                    enabled(%s)
                }
            }

            dependencies {
                testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
                testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.3")
                testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.3")
            }

            test {
                useJUnitPlatform()
            }
            """
                        .formatted(
                                options.applyAutomaticallyAfterTestTask(),
                                options.camelMethodName(),
                                options.reverseTransformation()));
    }
}
