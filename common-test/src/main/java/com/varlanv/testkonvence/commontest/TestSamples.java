package com.varlanv.testkonvence.commontest;

import com.varlanv.testkonvence.commontest.sample.Samples;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestSamples {

    public static Samples testSamples() {
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
                        + "    @Test\r\n"
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
            );
    }
}
