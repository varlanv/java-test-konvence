package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.commontest.BaseTest;
import com.varlanv.testkonvence.commontest.DataTable;
import com.varlanv.testkonvence.commontest.FunctionalTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.SAME_THREAD)
@Tags({@Tag(BaseTest.FUNCTIONAL_TEST_TAG), @Tag(BaseTest.SLOW_TEST_TAG)})
public class TestKonvencePluginFunctionalTest implements FunctionalTest {

    @ParameterizedTest
    @MethodSource("defaultDataTables")
    void abc(DataTable dataTable) {
        runGradleRunnerFixture(
            dataTable,
            List.of("test"),
            (fixture) -> {
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
                var javaDir = Files.createDirectories(fixture.subjectProjectDir().resolve("src").resolve("test").resolve("java").resolve("somepackage"));
                var someTestClass = Files.createFile(javaDir.resolve("SomeTest.java"));
                Files.writeString(
                    someTestClass,
                    java("""
                        package somepackage;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("Some test")
                            void someMethodTest() {
                                System.err.println("someMethodTest");
                            }
                        }
                        """
                    )
                );
                Files.writeString(
                    fixture.settingsFile(),
                    groovy("""
                        rootProject.name = "functional-test"
                        """)
                );

                var result = build(fixture.runner());

                assertThat(result.getOutput()).contains("Hello!");
                var s = Files.readString(someTestClass);
                System.out.println(s);

                System.out.println(
                    FileUtils.listFilesAndDirs(
                            fixture.subjectProjectDir().toFile(),
                            TrueFileFilter.INSTANCE,
                            TrueFileFilter.INSTANCE
                        ).stream()
                        .map(it -> it.getAbsolutePath() + System.lineSeparator())
                        .collect(Collectors.joining())
                );

                var enforcementsXml = fixture.subjectProjectDir()
                    .resolve("build")
                    .resolve("generated")
                    .resolve("sources")
                    .resolve("annotationProcessor")
                    .resolve("java")
                    .resolve("test")
                    .resolve("com")
                    .resolve("varlanv")
                    .resolve("testkonvence")
                    .resolve("testkonvence_enforcements.xml");
                System.out.println(Files.readString(enforcementsXml));
            }
        );
    }
}
