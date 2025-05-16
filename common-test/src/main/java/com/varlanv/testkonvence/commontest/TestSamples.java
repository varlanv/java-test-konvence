package com.varlanv.testkonvence.commontest;

import com.varlanv.testkonvence.commontest.sample.Samples;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestSamples {

    public static Samples testSamples() {
        return Stream.of(inlineTestSamples(), samplesFromResources())
            .reduce(Samples.samples(), Samples::merge, (a, b) -> b);
    }

    @SuppressWarnings("all")
    private static Samples inlineTestSamples() {
        return Samples.samples()
            .describe(
                "Should replace method name if found",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("Some display name")
                            void someTest() {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
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
                    ))
            .describe(
                "Should not add newline if source file did not have it",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("Some display name")
                            void someTest() {
                            }
                        }"""
                    )
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("Some display name")
                            void some_display_name() {
                            }
                        }"""
                    ))
            .describe(
                "Should not change newlines in the end of file if there are multiple newlines",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("Some display name")
                            void someTest() {
                            }
                        }
                        
                        
                        
                        """
                    )
                    .withExpectedTransformation("""
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
                    ))
            .describe(
                "Should replace method name to camel case if found and requested camel",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("Some display name")
                            void someTest() {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("Some display name")
                            void someDisplayName() {
                            }
                        }
                        """
                    )
                    .withOptions(options -> options.camelMethodName(true)))
            .describe(
                "Should replace only one method name if there is two methods, one is for replacement, and one method name is subset of other",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("Some display name 1")
                            void someTest() {
                            }
                        
                            @Test
                            @DisplayName("Some display name 2")
                            void someTestTwo() {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("Some display name 1")
                            void some_display_name_1() {
                            }
                        
                            @Test
                            @DisplayName("Some display name 2")
                            void some_display_name_2() {
                            }
                        }
                        """))
            .describe(
                "Should replace only one method name if there are three methods, each are subset of other and all for replacement",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("qwe1")
                            void a() {
                            }
                        
                            @Test
                            @DisplayName("qwe2")
                            void ab() {
                            }
                        
                            @Test
                            @DisplayName("qwe3")
                            void abc() {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("qwe1")
                            void qwe1() {
                            }
                        
                            @Test
                            @DisplayName("qwe2")
                            void qwe2() {
                            }
                        
                            @Test
                            @DisplayName("qwe3")
                            void qwe3() {
                            }
                        }
                        """))
            .describe(
                """
                    When one method has display name that will be transformed to method name that already exists in class,
                    and other method will be transformed to different name, then should rename both methods correctly""",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("ab")
                            void a() {
                            }
                        
                            @Test
                            @DisplayName("qwe")
                            void ab() {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("ab")
                            void ab() {
                            }
                        
                            @Test
                            @DisplayName("qwe")
                            void qwe() {
                            }
                        }
                        """))
            .describe(
                "Should replace only one method name if there is two methods, one is for replacement, and one method name is subset of other - reversed method order",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("Some display name 2")
                            void someTestTwo() {
                            }
                        
                            @Test
                            @DisplayName("Some display name 1")
                            void someTest() {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("Some display name 2")
                            void some_display_name_2() {
                            }
                        
                            @Test
                            @DisplayName("Some display name 1")
                            void some_display_name_1() {
                            }
                        }
                        """))
            .describe(
                "Should replace method name if found and method name not indented",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("Some display name")
                            void
                            someTest
                            (
                            )
                            {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("Some display name")
                            void
                            some_display_name
                            (
                            )
                            {
                            }
                        }
                        """
                    ))
            .describe(
                "Should preserver CRLF line separator when replacing method name",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources(""
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
                        + "}\r\n")
                    .withExpectedTransformation(""
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
                        + "}\r\n")
            )
            .describe(
                "Should preserve LF line separator when replacing method name",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources(""
                        + "package testcases;\n"
                        + "\n"
                        + "import org.junit.jupiter.api.DisplayName;\n"
                        + "import org.junit.jupiter.api.Test;\n"
                        + "\n"
                        + "class SomeTest {\n"
                        + "\n"
                        + "    @Test\n"
                        + "    @DisplayName(\"Some display name\")\n"
                        + "    void someTest() {\n"
                        + "    }\n"
                        + "}\n")
                    .withExpectedTransformation(""
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
                        + "}\n")
            )
            .describe(
                "Should not change sources if match not found",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("Some display name")
                            void some_display_name() {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("Some display name")
                            void some_display_name() {
                            }
                        }
                        """)
            )
            .describe(
                "Should replace method name only for class where method defined",
                spec -> spec
                    .withClass("testcases.SomeOuterTest")
                    .withJavaSources("""
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
                        """)
                    .withExpectedTransformation("""
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
                    )
            )
            .describe(
                "Should replace both method names if requested",
                spec -> spec
                    .withClass("testcases.SomeOuterTest")
                    .withJavaSources("""
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
                        """)
                    .withExpectedTransformation("""
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
                    )
            )
            .describeMany(
                Stream.of(
                        Map.entry("Some good test name", "some_good_test_name"),
                        Map.entry("Some good test name, which has comas, dots., question? marks, and exclamation marks!",
                            "some_good_test_name_which_has_comas_dots_question_marks_and_exclamation_marks"),
                        Map.entry("Some123good456test789name0", "some123good456test789name0"),
                        Map.entry("___abc___", "abc"),
                        Map.entry("a_b_c_", "a_b_c"),
                        Map.entry("a_b_c", "a_b_c"),
                        Map.entry("-a_b_c-", "a_b_c"),
                        Map.entry("a_b_c-", "a_b_c"),
                        Map.entry("-a_b_c", "a_b_c")
                    )
                    .map(entry -> Map.entry(
                            "Test method name \"someTest\" should be converted to method name \"%s\" when display name is \"%s\""
                                .formatted(entry.getKey(), entry.getValue()),
                            spec -> spec
                                .withClass("testcases.SomeTest")
                                .withJavaSources("""
                                    package testcases;
                                    
                                    import org.junit.jupiter.api.DisplayName;
                                    import org.junit.jupiter.api.Test;
                                    
                                    class SomeTest {
                                    
                                        @Test
                                        @DisplayName("%s")
                                        void someTest() {
                                        }
                                    }
                                    """.formatted(entry.getKey()))
                                .withExpectedTransformation("""
                                    package testcases;
                                    
                                    import org.junit.jupiter.api.DisplayName;
                                    import org.junit.jupiter.api.Test;
                                    
                                    class SomeTest {
                                    
                                        @Test
                                        @DisplayName("%s")
                                        void %s() {
                                        }
                                    }
                                    """.formatted(entry.getKey(), entry.getValue()))
                        )
                    )
            )
            .describe(
                "Should replace method name only in one source when there are two source file and one has method name for replacement",
                spec -> spec
                    .withClass("testcases.SomeFirstTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeFirstTest {
                        
                            @Test
                            @DisplayName("Some test")
                            void some_test() {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeFirstTest {
                        
                            @Test
                            @DisplayName("Some test")
                            void some_test() {
                            }
                        }
                        """
                    )
                    .and()
                    .withClass("testcases.SomeSecondTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeSecondTest {
                        
                            @Test
                            @DisplayName("Some test name")
                            void some_test() {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeSecondTest {
                        
                            @Test
                            @DisplayName("Some test name")
                            void some_test_name() {
                            }
                        }
                        """
                    )
            )
            .describe(
                "Should add @DisplayName if requested reverseTransformation and @DisplayName not present",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            void some_test() {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("some test")
                            void some_test() {
                            }
                        }
                        """
                    ).withOptions(options -> options.reverseTransformation(true)))
            .describe(
                "Requested reverseTransformation and @DisplayName not present and already has import on @DisplayName",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            void some_test() {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.Test;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("some test")
                            void some_test() {
                            }
                        }
                        """
                    ).withOptions(options -> options.reverseTransformation(true)))
            .describe(
                "Requested reverseTransformation and @DisplayName not present and already has import on whole junit package",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.*;
                        
                        class SomeTest {
                        
                            @Test
                            void some_test() {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.*;
                        
                        class SomeTest {
                        
                            @Test
                            @DisplayName("some test")
                            void some_test() {
                            }
                        }
                        """
                    ).withOptions(options -> options.reverseTransformation(true)))
            .describe(
                "Requested reverseTransformation and @DisplayName not present and @ParameterizedTest",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.params.ParameterizedTest;
                        import org.junit.jupiter.params.provider.ValueSource;
                        
                        class SomeTest {
                        
                            @ParameterizedTest
                            @ValueSource(ints = {1, 2, 3})
                            void some_test(int value) {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.params.ParameterizedTest;
                        import org.junit.jupiter.params.provider.ValueSource;
                        
                        class SomeTest {
                        
                            @ParameterizedTest
                            @ValueSource(ints = {1, 2, 3})
                            @DisplayName("some test")
                            void some_test(int value) {
                            }
                        }
                        """
                    ).withOptions(options -> options.reverseTransformation(true)))
            .describe(
                "Requested reverseTransformation and @DisplayName not present and @TestFactory",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.TestFactory;
                        
                        class SomeTest {
                        
                            @TestFactory
                            void some_test() {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.TestFactory;
                        
                        class SomeTest {
                        
                            @TestFactory
                            @DisplayName("some test")
                            void some_test() {
                            }
                        }
                        """
                    ).withOptions(options -> options.reverseTransformation(true)))
            .describe(
                "Requested reverseTransformation and @DisplayName not present and @RepeatedTest",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.RepeatedTest;
                        
                        class SomeTest {
                        
                            @RepeatedTest(10)
                            void some_test() {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.RepeatedTest;
                        
                        class SomeTest {
                        
                            @RepeatedTest(10)
                            @DisplayName("some test")
                            void some_test() {
                            }
                        }
                        """
                    ).withOptions(options -> options.reverseTransformation(true)))
            .describe(
                "Requested reverseTransformation and @DisplayName not present and @Test and some other annotations",
                spec -> spec
                    .withClass("testcases.SomeTest")
                    .withJavaSources("""
                        package testcases;
                        
                        import org.junit.jupiter.api.RepeatedTest;
                        import org.junit.jupiter.api.Tag;
                        
                        class SomeTest {
                        
                            @SuppressWarnings("all")
                            @Tag("some_tag")
                            @RepeatedTest(10)
                            void some_test() {
                            }
                        }
                        """)
                    .withExpectedTransformation("""
                        package testcases;
                        
                        import org.junit.jupiter.api.DisplayName;
                        import org.junit.jupiter.api.RepeatedTest;
                        import org.junit.jupiter.api.Tag;
                        
                        class SomeTest {
                        
                            @SuppressWarnings("all")
                            @Tag("some_tag")
                            @RepeatedTest(10)
                            @DisplayName("some test")
                            void some_test() {
                            }
                        }
                        """
                    ).withOptions(options -> options.reverseTransformation(true)));
    }


    @SneakyThrows
    @SuppressWarnings("all")
    private static Samples samplesFromResources() {
        var samples = Samples.samples();
        var className = "JsonAssuredTest";
        var packageLen = "package ".length();

        try (var actualIs = Objects.requireNonNull(Samples.class.getResourceAsStream(
            "/testkonvence/samples/jsonassured_1/actual/" + className + ".java"));
             var expectedIs = Objects.requireNonNull(Samples.class.getResourceAsStream(
                 "/testkonvence/samples/jsonassured_1/expected/" + className + ".java"
             ))) {
            var actualString = new String(actualIs.readAllBytes(), StandardCharsets.UTF_8);
            var expectedString = new String(expectedIs.readAllBytes(), StandardCharsets.UTF_8);
            var packageEndIdx = actualString.indexOf(";");
            var packageName = actualString.substring(packageLen, packageEndIdx);
            var fullyQualifiedClassName = packageName + "." + className;

            samples.describe(className, spec -> spec
                .withClass(fullyQualifiedClassName)
                .withJavaSources(actualString)
                .withExpectedTransformation(expectedString)
                .withOptions(options -> options.reverseTransformation(true)));
        }


        return samples;
    }
}
