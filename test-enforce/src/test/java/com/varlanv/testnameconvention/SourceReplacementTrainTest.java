//package com.varlanv.testnameconvention;
//
//import com.varlanv.testnameconvention.commontest.UnitTest;
//import org.intellij.lang.annotations.Language;
//import org.junit.jupiter.api.DynamicTest;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestFactory;
//
//import java.nio.file.Paths;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class SourceReplacementTrainTest implements UnitTest {
//
//    @Nested
//    class OneClassOneMethod {
//
//        @Test
//        void should_replace_method_name_if_found() {
//            spec(
//                """
//                    package somePackage;
//
//                    import org.junit.jupiter.api.DisplayName;
//                    import org.junit.jupiter.api.Test;
//
//                    class SomeTest {
//
//                        @Test
//                        @DisplayName("Some display name")
//                        void someTest() {
//                        }
//                    }
//                    """,
//                "SomeTest",
//                new MethodNameFromDisplayName("Some display name", "someTest"),
//                """
//                    package somePackage;
//
//                    import org.junit.jupiter.api.DisplayName;
//                    import org.junit.jupiter.api.Test;
//
//                    class SomeTest {
//
//                        @Test
//                        @DisplayName("Some display name")
//                        void some_display_name() {
//                        }
//                    }
//                    """
//            );
//        }
//
//        @Test
//        void should_replace_class_name_if_found() {
//            spec(
//                """
//                    package somePackage;
//
//                    import org.junit.jupiter.api.DisplayName;
//                    import org.junit.jupiter.api.Test;
//
//                    @DisplayName("Some class display name")
//                    class SomeTest {
//
//                        @Test
//                        @DisplayName("Some display name")
//                        void someTest() {
//                        }
//                    }
//                    """,
//                "SomeTest",
//                new ClassNameFromDisplayName("Some class display name", "SomeTest"),
//                """
//                    package somePackage;
//
//                    import org.junit.jupiter.api.DisplayName;
//                    import org.junit.jupiter.api.Test;
//
//                    @DisplayName("Some class display name")
//                    class SomeClassDisplayNameTest {
//
//                        @Test
//                        @DisplayName("Some display name")
//                        void someTest() {
//                        }
//                    }
//                    """
//            );
//        }
//
//        @Test
//        void should_not_change_sources_if_match_not_found() {
//            @Language("Java")
//            var sources = """
//                package somePackage;
//
//                import org.junit.jupiter.api.DisplayName;
//                import org.junit.jupiter.api.Test;
//
//                class SomeTest {
//
//                    @Test
//                    @DisplayName("Some display name")
//                    void someTestNameThanShouldNotMatch() {
//                    }
//                }
//                """;
//            spec(
//                sources,
//                "SomeTest",
//                new MethodNameFromDisplayName("Some display name", "someTest"),
//                sources
//            );
//        }
//
//        @TestFactory
//        Stream<DynamicTest> should_change_source_if_display_name_can_be_converted_to_test_name() {
//            return Stream.of(
//                    Map.entry("Some good test name", "some_good_test_name"),
//                    Map.entry("Some good test name, which has comas, dots., question? marks, and exclamation marks!", "some_good_test_name_which_has_comas_dots_question_marks_and_exclamation_marks"),
//                    Map.entry("Some123good456test789name0", "some123good456test789name0"),
//                    Map.entry("___abc___", "abc"),
//                    Map.entry("a_b_c_", "a_b_c"),
//                    Map.entry("a_b_c", "a_b_c"),
//                    Map.entry("-a_b_c-", "a_b_c"),
//                    Map.entry("a_b_c-", "a_b_c"),
//                    Map.entry("-a_b_c", "a_b_c"),
//                    Map.entry("Some good\n multiline   test\n name, which also has multiple whitespaces and_underscores   ", "some_good_multiline_test_name_which_also_has_multiple_whitespaces_and_underscores")
//                )
//                .map(entry -> DynamicTest.dynamicTest(
//                        "Test method name \"someTest\" should be converted to method name \"%s\"".formatted(entry.getValue()),
//                        () -> {
//                            var displayName = entry.getKey();
//                            var expectedMethodName = entry.getValue();
//                            @Language("Java")
//                            var sources = """
//                                package somePackage;
//
//                                import org.junit.jupiter.api.DisplayName;
//                                import org.junit.jupiter.api.Test;
//
//                                class SomeTest {
//
//                                    @Test
//                                    @DisplayName("%s")
//                                    void someTest() {
//                                    }
//                                }
//                                """;
//                            spec(
//                                sources.formatted(displayName),
//                                "SomeTest",
//                                new MethodNameFromDisplayName(displayName, "someTest"),
//                                """
//                                    package somePackage;
//
//                                    import org.junit.jupiter.api.DisplayName;
//                                    import org.junit.jupiter.api.Test;
//
//                                    class SomeTest {
//
//                                        @Test
//                                        @DisplayName("%s")
//                                        void %s() {
//                                        }
//                                    }
//                                    """.formatted(displayName, expectedMethodName)
//                            );
//                        }
//                    )
//                );
//        }
//    }
//
//    @Nested
//    class OneNestedClassOneOuterOneNestedMethod {
//
//        @Test
//        void should_replace_method_name_only_for_class_where_method_defined() {
//            spec(
//                """
//                    package somePackage;
//
//                    import org.junit.jupiter.api.DisplayName;
//                    import org.junit.jupiter.api.Nested;
//                    import org.junit.jupiter.api.Test;
//
//                    class SomeOuterTest {
//
//                        @Test
//                        @DisplayName("Some display name")
//                        void someTest() {
//                        }
//
//                        @Nested
//                        class SomeNestedTest {
//
//                            @Test
//                            @DisplayName("Some display name")
//                            void someTest() {
//                            }
//                        }
//                    }
//                    """,
//                "SomeNestedTest",
//                new MethodNameFromDisplayName("Some display name", "someTest"),
//                """
//                    package somePackage;
//
//                    import org.junit.jupiter.api.DisplayName;
//                    import org.junit.jupiter.api.Nested;
//                    import org.junit.jupiter.api.Test;
//
//                    class SomeOuterTest {
//
//                        @Test
//                        @DisplayName("Some display name")
//                        void someTest() {
//                        }
//
//                        @Nested
//                        class SomeNestedTest {
//
//                            @Test
//                            @DisplayName("Some display name")
//                            void some_display_name() {
//                            }
//                        }
//                    }
//                    """);
//        }
//
//        @Test
//        void should_replace_both_method_names_if_requested() {
//            spec(
//                """
//                    package somePackage;
//
//                    import org.junit.jupiter.api.DisplayName;
//                    import org.junit.jupiter.api.Nested;
//                    import org.junit.jupiter.api.Test;
//
//                    class SomeOuterTest {
//
//                        @Test
//                        @DisplayName("Some display name")
//                        void someTest() {
//                        }
//
//                        @Nested
//                        class SomeNestedTest {
//
//                            @Test
//                            @DisplayName("Some display name")
//                            void someTest() {
//                            }
//                        }
//                    }
//                    """,
//                List.of(
//                    Map.entry("SomeNestedTest", new MethodNameFromDisplayName("Some display name", "someTest")),
//                    Map.entry("SomeOuterTest", new MethodNameFromDisplayName("Some display name", "someTest"))
//                ),
//                """
//                    package somePackage;
//
//                    import org.junit.jupiter.api.DisplayName;
//                    import org.junit.jupiter.api.Nested;
//                    import org.junit.jupiter.api.Test;
//
//                    class SomeOuterTest {
//
//                        @Test
//                        @DisplayName("Some display name")
//                        void some_display_name() {
//                        }
//
//                        @Nested
//                        class SomeNestedTest {
//
//                            @Test
//                            @DisplayName("Some display name")
//                            void some_display_name() {
//                            }
//                        }
//                    }
//                    """);
//        }
//    }
//
//    void spec(@Language("Java") String sourceContent,
//              List<Map.Entry<String, EnforceCandidate>> immediateClassNameWithEnforceCandidate,
//              @Language("Java") String expected) {
//        useTempDir(tempDir -> {
//            var memorySourceFile = new MemorySourceFile(Paths.get("somePath"), sourceContent);
//            var subject = new SourceReplacementTrain(
//                new EnforcementMeta(
//                    immediateClassNameWithEnforceCandidate.stream()
//                        .map(entry ->
//                            new EnforcementMeta.Item(
//                                memorySourceFile,
//                                entry.getKey(),
//                                entry.getValue()
//                            )
//                        )
//                        .toList()
//                )
//            );
//            subject.run();
//            var actual = memorySourceFile.lines().stream()
//                .map(line -> line + System.lineSeparator())
//                .collect(Collectors.joining());
//            assertThat(actual).isEqualTo(expected);
//        });
//    }
//
//    void spec(@Language("Java") String sourceContent,
//              String immediateClassName,
//              EnforceCandidate enforceCandidate,
//              @Language("Java") String expected) {
//        spec(sourceContent, List.of(Map.entry(immediateClassName, enforceCandidate)), expected);
//    }
//}
