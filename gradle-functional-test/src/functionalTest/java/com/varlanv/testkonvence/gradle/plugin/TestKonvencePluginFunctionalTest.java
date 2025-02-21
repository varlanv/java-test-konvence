package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.commontest.*;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.SAME_THREAD)
@Tags({@Tag(BaseTest.FUNCTIONAL_TEST_TAG), @Tag(BaseTest.SLOW_TEST_TAG)})
class TestKonvencePluginFunctionalTest implements FunctionalTest {

    @TestFactory
    Stream<DynamicTest> fromSamples() {
        return TestSamples.samples().stream()
            .map(sample -> DynamicTest.dynamicTest(sample.description(), () -> {
                sample.consume(consumableSample -> {
                    runGradleRunnerFixture(
                        new DataTable(false, false, false, TestGradleVersions.current()),
                        List.of("test"),
                        (fixture) -> {
                            Files.writeString(
                                fixture.settingsFile(),
                                groovy("""
                                    rootProject.name = "functional-test"
                                    """)
                            );

                            Files.writeString(
                                fixture.rootBuildFile(),
                                groovy("""
                                    plugins {
                                        id("java")
                                        id("com.varlanv.testkonvence-gradle-plugin")
                                    }
                                    
                                    repositories {
                                        mavenLocal()
                                        mavenCentral()
                                    }
                                    
                                    dependencies {
                                        testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
                                        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.3")
                                    }
                                    
                                    test {
                                        useJUnitPlatform()
                                    }
                                    """
                                )
                            );
                            var javaDir = Files.createDirectories(fixture.subjectProjectDir().resolve("src").resolve("test").resolve("java"));
                            for (var sampleSourceFile : consumableSample.sources()) {
                                var relativeSourceFilePath = consumableSample.dir().relativize(sampleSourceFile.path());
                                var sourceFile = javaDir.resolve(relativeSourceFilePath);
                                Files.createDirectories(sourceFile.getParent());
                                Files.move(sampleSourceFile.path(), sourceFile);
                            }
                            build(fixture.runner());
                            for (var sampleSourceFile : consumableSample.sources()) {
                                var relativeSourceFilePath = consumableSample.dir().relativize(sampleSourceFile.path());
                                var sourceFile = javaDir.resolve(relativeSourceFilePath);
                                var modifiedSourceFileContent = Files.readString(sourceFile);
                                assertThat(modifiedSourceFileContent).isEqualTo(sampleSourceFile.expectedTransformation());
                            }

//                            System.out.println(
//                                FileUtils.listFilesAndDirs(
//                                        fixture.subjectProjectDir().toFile(),
//                                        TrueFileFilter.INSTANCE,
//                                        TrueFileFilter.INSTANCE
//                                    ).stream()
//                                    .map(it -> it.getAbsolutePath() + System.lineSeparator())
//                                    .collect(Collectors.joining())
//                            );
                        }
                    );
                });
            }));
    }
}
