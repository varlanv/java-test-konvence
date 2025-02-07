package com.varlanv.testnameconvention;

import com.varlanv.testnameconvention.commontest.IntegrationTest;
import com.varlanv.testnameconvention.info.XmlEnforceMeta;
import com.varlanv.testnameconvention.proc.TestNameConventionAP;
import io.toolisticon.cute.Cute;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import javax.tools.StandardLocation;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
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
        var items = new XmlEnforceMeta().items(new ByteArrayInputStream(resultXml.getBytes(StandardCharsets.UTF_8)));
        var memorySourceFile = new MemorySourceFile("somePath", sourceContent);
        var subject = new SourceReplacementTrain(
            new EnforcementMeta(
                items.stream().map(item -> {
                            EnforceCandidate candidate;
                            var classNameParts = item.className().split("\\.");
                            var className = classNameParts[classNameParts.length - 1];
                            if (item.methodName().isEmpty()) {
                                candidate = new ClassNameFromDisplayName(item.displayName(), className);
                            } else {
                                candidate = new MethodNameFromDisplayName(item.displayName(), item.methodName());
                            }
                            return new EnforcementMeta.Item(
                                memorySourceFile,
                                className,
                                candidate
                            );
                        }
                    )
                    .toList()
            )
        );
        subject.run();
        var actual = memorySourceFile.lines().stream()
            .map(line -> line + System.lineSeparator())
            .collect(Collectors.joining());
        assertThat(actual).isEqualTo(expected);

    }

    String runAnnotationProcessor(String className, @Language("Java") String sources) {
        return runAnnotationProcessor(Map.of(className, sources));
    }

    String runAnnotationProcessor(Map<String, String> sources) {
        var iterator = sources.entrySet().iterator();
        var first = iterator.next();
        var cute = Cute
            .blackBoxTest()
            .given()
            .processor(TestNameConventionAP.class)
            .andSourceFile(first.getKey(), first.getValue());
        while (iterator.hasNext()) {
            var next = iterator.next();
            cute = cute.andSourceFile(next.getKey(), next.getValue());
        }
        var resultXml = new AtomicReference<String>();
        cute.andUseCompilerOptions("-A" + TestNameConventionAP.indentXmlOption + "=false").whenCompiled()
            .thenExpectThat().compilationSucceeds()
            .andThat().fileObject(StandardLocation.SOURCE_OUTPUT, TestNameConventionAP.enforcementsXmlPackage, TestNameConventionAP.enforcementsXmlName).exists()
            .executeTest()
            .executeCustomAssertions(outcome -> {
                resultXml.set(outcome.getFileManager().getFileObjects().get(0).getContent());
            });
        assertThat(resultXml.get()).isNotBlank();
        return resultXml.get();
    }
}
