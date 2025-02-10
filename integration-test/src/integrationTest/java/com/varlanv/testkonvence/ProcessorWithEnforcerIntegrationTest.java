package com.varlanv.testkonvence;

import com.varlanv.testkonvence.commontest.IntegrationTest;
import com.varlanv.testkonvence.proc.TestKonvenceAP;
import io.toolisticon.cute.Cute;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import javax.tools.StandardLocation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessorWithEnforcerIntegrationTest implements IntegrationTest {

    @Nested
    class OneClassOneMethod implements IntegrationTest {

        @Test
        void should_replace_method_name_if_found() {
            spec(
                "testcases.SomeTest",
                """
                    package testcases;
                    
                    import org.junit.jupiter.api.DisplayName;
                    import org.junit.jupiter.api.Test;
                    
                    class SomeTest {
                    
                        @Test
                        @DisplayName("Some display name")
                        void someTest() {
                        }
                    }
                    """,
                """
                    package testcases;
                    
                    import org.junit.jupiter.api.DisplayName;
                    import org.junit.jupiter.api.Test;
                    
                    class SomeTest {
                    
                        @Test
                        @DisplayName("Some display name")
                        void some_display_name() {
                        }
                    }
                    """
            );
        }

        @Test
        void should_preserve_crlf_line_separator_when_replacing_method_name() {
            @Language("Java")
            var sourceContent = ""
                + "package testcases;\r\n"
                + "\r\n"
                + "import org.junit.jupiter.api.DisplayName;\r\n"
                + "import org.junit.jupiter.api.Test;\r\n"
                + "\r\n"
                + "class SomeTest {\r\n"
                + "\r\n"
                + "    @Test\r\n"
                + "    @DisplayName(\"Some display name\")\r\n"
                + "    void someTest() {\r\n"
                + "    }\r\n"
                + "}\r\n";

            var expected = ""
                + "package testcases;\r\n"
                + "\r\n"
                + "import org.junit.jupiter.api.DisplayName;\r\n"
                + "import org.junit.jupiter.api.Test;\r\n"
                + "\r\n"
                + "class SomeTest {\r\n"
                + "\r\n"
                + "    @Test\r\n"
                + "    @DisplayName(\"Some display name\")\r\n"
                + "    void some_display_name() {\r\n"
                + "    }\r\n"
                + "}\r\n";

            spec(
                "testcases.SomeTest",
                sourceContent,
                expected
            );


        }

        @Test
        void should_preserve_lf_line_separator_when_replacing_method_name() {
            @Language("Java")
            var sourceContent = ""
                + "package testcases;\n"
                + "\n"
                + "import org.junit.jupiter.api.DisplayName;\n"
                + "import org.junit.jupiter.api.Test;\n"
                + "\n"
                + "class SomeTest {\n"
                + "\n"
                + "    @Test\r\n"
                + "    @DisplayName(\"Some display name\")\n"
                + "    void someTest() {\n"
                + "    }\n"
                + "}\n";

            var expected = ""
                + "package testcases;\n"
                + "\n"
                + "import org.junit.jupiter.api.DisplayName;\n"
                + "import org.junit.jupiter.api.Test;\n"
                + "\n"
                + "class SomeTest {\n"
                + "\n"
                + "    @Test\n"
                + "    @DisplayName(\"Some display name\")\n"
                + "    void some_display_name() {\n"
                + "    }\n"
                + "}\n";

            spec(
                "testcases.SomeTest",
                sourceContent,
                expected
            );
        }

        @Test
        void should_replace_class_name_if_found() {
            spec(
                "testcases.SomeTest",
                """
                    package testcases;
                    
                    import org.junit.jupiter.api.DisplayName;
                    import org.junit.jupiter.api.Test;
                    
                    @DisplayName("Some class display name")
                    class SomeTest {
                    
                        @Test
                        void someTest() {
                        }
                    }
                    """,
                """
                    package testcases;
                    
                    import org.junit.jupiter.api.DisplayName;
                    import org.junit.jupiter.api.Test;
                    
                    @DisplayName("Some class display name")
                    class SomeClassDisplayNameTest {
                    
                        @Test
                        void someTest() {
                        }
                    }
                    """
            );
        }

        @Test
        void should_not_change_sources_if_match_not_found() {
            @Language("Java")
            var sources = """
                package testcases;
                
                import org.junit.jupiter.api.DisplayName;
                import org.junit.jupiter.api.Test;
                
                class SomeTest {
                
                    @Test
                    @DisplayName("Some display name")
                    void some_display_name() {
                    }
                }
                """;
            spec(
                "testcases.SomeTest",
                sources,
                sources
            );
        }

        @TestFactory
        Stream<DynamicTest> should_change_source_if_display_name_can_be_converted_to_test_name() {
            return Stream.of(
                    Map.entry("Some good test name", "some_good_test_name"),
                    Map.entry("Some good test name, which has comas, dots., question? marks, and exclamation marks!", "some_good_test_name_which_has_comas_dots_question_marks_and_exclamation_marks"),
                    Map.entry("Some123good456test789name0", "some123good456test789name0"),
                    Map.entry("___abc___", "abc"),
                    Map.entry("a_b_c_", "a_b_c"),
                    Map.entry("a_b_c", "a_b_c"),
                    Map.entry("-a_b_c-", "a_b_c"),
                    Map.entry("a_b_c-", "a_b_c"),
                    Map.entry("-a_b_c", "a_b_c")
                )
                .map(entry -> DynamicTest.dynamicTest(
                        "Test method name \"someTest\" should be converted to method name \"%s\" when display name is \"%s\"".formatted(entry.getValue(), entry.getKey()),
                        () -> {
                            var displayName = entry.getKey();
                            var expectedMethodName = entry.getValue();
                            @Language("Java")
                            var sources = """
                                package testcases;
                                
                                import org.junit.jupiter.api.DisplayName;
                                import org.junit.jupiter.api.Test;
                                
                                class SomeTest {
                                
                                    @Test
                                    @DisplayName("%s")
                                    void someTest() {
                                    }
                                }
                                """;
                            spec(
                                "testcases.SomeTest",
                                sources.formatted(displayName),
                                """
                                    package testcases;
                                    
                                    import org.junit.jupiter.api.DisplayName;
                                    import org.junit.jupiter.api.Test;
                                    
                                    class SomeTest {
                                    
                                        @Test
                                        @DisplayName("%s")
                                        void %s() {
                                        }
                                    }
                                    """.formatted(displayName, expectedMethodName)
                            );
                        }
                    )
                );
        }
    }

    @Nested
    class OneNestedClassOneOuterOneNestedMethod implements IntegrationTest {

        @Test
        void should_replace_method_name_only_for_class_where_method_defined() {
            spec(
                "testcases.SomeOuterTest",
                """
                    package testcases;
                    
                    import org.junit.jupiter.api.DisplayName;
                    import org.junit.jupiter.api.Nested;
                    import org.junit.jupiter.api.Test;
                    
                    class SomeOuterTest {
                    
                        @Test
                        @DisplayName("Some display name1")
                        void someTest() {
                        }
                    
                        @Nested
                        class SomeNestedTest {
                    
                            @Test
                            @DisplayName("Some display name2")
                            void someTest() {
                            }
                        }
                    }
                    """,
                """
                    package testcases;
                    
                    import org.junit.jupiter.api.DisplayName;
                    import org.junit.jupiter.api.Nested;
                    import org.junit.jupiter.api.Test;
                    
                    class SomeOuterTest {
                    
                        @Test
                        @DisplayName("Some display name1")
                        void some_display_name1() {
                        }
                    
                        @Nested
                        class SomeNestedTest {
                    
                            @Test
                            @DisplayName("Some display name2")
                            void some_display_name2() {
                            }
                        }
                    }
                    """
            );
        }

        @Test
        void should_replace_both_method_names_if_requested() {
            spec(
                "testcases.SomeOuterTest",
                """
                    package testcases;
                    
                    import org.junit.jupiter.api.DisplayName;
                    import org.junit.jupiter.api.Nested;
                    import org.junit.jupiter.api.Test;
                    
                    class SomeOuterTest {
                    
                        @Test
                        @DisplayName("Some display name")
                        void some_display_name() {
                        }
                    
                        @Nested
                        class SomeNestedTest {
                    
                            @Test
                            @DisplayName("Some display name")
                            void some_display_name() {
                            }
                        }
                    }
                    """,
                """
                    package testcases;
                    
                    import org.junit.jupiter.api.DisplayName;
                    import org.junit.jupiter.api.Nested;
                    import org.junit.jupiter.api.Test;
                    
                    class SomeOuterTest {
                    
                        @Test
                        @DisplayName("Some display name")
                        void some_display_name() {
                        }
                    
                        @Nested
                        class SomeNestedTest {
                    
                            @Test
                            @DisplayName("Some display name")
                            void some_display_name() {
                            }
                        }
                    }
                    """
            );
        }
    }

    void spec(String outerClassName,
              @Language("Java") String sourceContent,
              @Language("Java") String expected) {
        var resultXml = runAnnotationProcessor(outerClassName, sourceContent);
        useTempDir(sourceRoot -> {
            var outerClassNameParts = outerClassName.split("\\.");
            var currentPackage = sourceRoot;
            var sourcesFile = new AtomicReference<Path>();
            for (var i = 0; i < outerClassNameParts.length; i++) {
                var isLast = i == outerClassNameParts.length - 1;
                if (isLast) {
                    sourcesFile.set(Files.writeString(currentPackage.resolve(outerClassNameParts[i]), sourceContent, StandardOpenOption.CREATE_NEW));
                } else {
                    currentPackage = Files.createDirectory(currentPackage.resolve(outerClassNameParts[i]));
                }
            }
            assertThat(sourcesFile).isNotNull();
            useTempFile(resultXmlPath -> {
                Files.write(resultXmlPath, resultXml);
                new Train(
                    resultXmlPath,
                    sourceRoot,
                    List.of(sourcesFile.get())
                ).run();

                var actual = Files.readString(sourcesFile.get());
                assertThat(actual).isEqualTo(expected);
            });
        });
    }

    byte[] runAnnotationProcessor(String className, @Language("Java") String sources) {
        return runAnnotationProcessor(Map.of(className, sources));
    }

    byte[] runAnnotationProcessor(Map<String, String> sources) {
        var iterator = sources.entrySet().iterator();
        var first = iterator.next();
        var cute = Cute
            .blackBoxTest()
            .given()
            .processor(TestKonvenceAP.class)
            .andSourceFile(first.getKey(), first.getValue());
        while (iterator.hasNext()) {
            var next = iterator.next();
            cute = cute.andSourceFile(next.getKey(), next.getValue());
        }
        var resultXml = new AtomicReference<byte[]>();
        cute.andUseCompilerOptions("-A" + TestKonvenceAP.indentXmlOption + "=false").whenCompiled()
            .thenExpectThat().compilationSucceeds()
            .andThat().fileObject(StandardLocation.SOURCE_OUTPUT, TestKonvenceAP.enforcementsXmlPackage, TestKonvenceAP.enforcementsXmlName).exists()
            .executeTest()
            .executeCustomAssertions(outcome -> {
                var fileManager = outcome.getFileManager();
                var fileObjects = fileManager.getFileObjects();
                assertThat(fileObjects).hasSize(1);
                resultXml.set(fileObjects.get(0).getContentAsByteArray());
            });
        assertThat(resultXml.get()).isNotEmpty();
        return resultXml.get();
    }
}
