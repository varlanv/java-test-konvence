package com.varlanv.testkonvence.proc;

import com.varlanv.testkonvence.commontest.UnitTest;
import io.toolisticon.cute.Cute;
import io.toolisticon.cute.CuteApi;
import io.toolisticon.cute.GeneratedFileObjectMatcher;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.tools.StandardLocation;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestKonvenceAPTest implements UnitTest {

    CuteApi.BlackBoxTestSourceFilesInterface cute = Cute
        .blackBoxTest()
        .given()
        .processor(TestKonvenceAP.class);

    @Nested
    class EmptyOutputFile implements UnitTest {

        @Test
        void when_doesnt_find_annotation__then_write_empty_file() {
            expectEmptyTransformation(
                "testcases.SomeTest",
                """
                    package testcases;
                    
                    public class SomeTest {
                    
                        void test() {
                        }
                    }
                    """
            );
        }
    }

    @Nested
    class SingleClassFileWithSingleTestMethod implements UnitTest {

        @Test
        void when_display_name_annotation_first_then_should_generate_output() {
            expectTransformation(
                "testcases.SomeTest",
                """
                    package testcases;
                    
                    import org.junit.jupiter.api.Test;
                    import org.junit.jupiter.api.DisplayName;
                    
                    public class SomeTest {
                    
                        @DisplayName("Some cool test name")
                        @Test
                        void test() {
                        }
                    }
                    """,
                """
                    <root>
                          <entry>
                              <fullEnclosingClassName>testcases.SomeTest</fullEnclosingClassName>
                              <displayName>Some cool test name</displayName>
                              <className>SomeTest</className>
                              <methodName>test</methodName>
                          </entry>
                      </root>
                    """
            );
        }

        @Test
        void when_test_annotation_first_then_should_generate_output() {
            expectTransformation(
                "testcases.SomeTest",
                """
                    package testcases;
                    
                    import org.junit.jupiter.api.Test;
                    import org.junit.jupiter.api.DisplayName;
                    
                    public class SomeTest {
                    
                        @Test
                        @DisplayName("Some cool test name")
                        void test() {
                        }
                    }
                    """,
                """
                    <root>
                          <entry>
                              <fullEnclosingClassName>testcases.SomeTest</fullEnclosingClassName>
                              <displayName>Some cool test name</displayName>
                              <className>SomeTest</className>
                              <methodName>test</methodName>
                          </entry>
                      </root>
                    """
            );
        }

        @Test
        void when_no_display_name_annotation_then_should_generate_output() {
            expectTransformation(
                "testcases.SomeTest",
                """
                    package testcases;
                    
                    import org.junit.jupiter.api.Test;
                    import org.junit.jupiter.api.DisplayName;
                    
                    public class SomeTest {
                    
                        @Test
                        void test() {
                        }
                    }
                    """,
                """
                    <root>
                          <entry>
                              <fullEnclosingClassName>testcases.SomeTest</fullEnclosingClassName>
                              <displayName/>
                              <className>SomeTest</className>
                              <methodName>test</methodName>
                          </entry>
                    </root>
                    """
            );
        }

        @Test
        void when_display_name_is_on_class_level__then_should_generate_output() {
            expectTransformation(
                "testcases.SomeTest",
                """
                    package testcases;
                    
                    import org.junit.jupiter.api.Test;
                    import org.junit.jupiter.api.DisplayName;
                    
                    @DisplayName("Some cool test class")
                    public class SomeTest {
                    
                        @Test
                        void test() {
                        }
                    }
                    """,
                """
                    <root>
                          <entry>
                              <fullEnclosingClassName>testcases.SomeTest</fullEnclosingClassName>
                              <displayName/>
                              <className>SomeTest</className>
                              <methodName>test</methodName>
                          </entry>
                          <entry>
                              <fullEnclosingClassName>testcases.SomeTest</fullEnclosingClassName>
                              <displayName>Some cool test class</displayName>
                              <className>testcases.SomeTest</className>
                              <methodName/>
                          </entry>
                    </root>
                    """
            );
        }

        @Test
        void when_display_name_is_on_parameterized_test__then_should_generate_output() {
            expectTransformation(
                "testcases.SomeTest",
                """
                    package testcases;
                    
                    import org.junit.jupiter.params.ParameterizedTest;
                    import org.junit.jupiter.params.provider.ValueSource;
                    
                    public class SomeTest {
                    
                        @ParameterizedTest
                        @ValueSource(strings = {""})
                        void test() {
                        }
                    }
                    """,
                """
                    <root>
                          <entry>
                              <fullEnclosingClassName>testcases.SomeTest</fullEnclosingClassName>
                              <displayName/>
                              <className>SomeTest</className>
                              <methodName>test</methodName>
                          </entry>
                    </root>
                    """
            );
        }
    }

    @Nested
    class SingleClassFileWithThreeTestMethods implements UnitTest {

        @Test
        void should_generate_correct_output_for_three_test_with_display_name() {
            expectTransformation(
                "testcases.SomeTest",
                """
                    package testcases;
                    
                    import org.junit.jupiter.api.Test;
                    import org.junit.jupiter.api.DisplayName;
                    
                    public class SomeTest {
                    
                        @Test
                        @DisplayName("Some cool test name 1")
                        void test1() {
                        }
                    
                        @Test
                        @DisplayName("Some cool test name 2")
                        void test2() {
                        }
                    
                        @Test
                        @DisplayName("Some cool test name 3")
                        void test3() {
                        }
                    }
                    """,
                """
                    <root>
                          <entry>
                              <fullEnclosingClassName>testcases.SomeTest</fullEnclosingClassName>
                              <displayName>Some cool test name 1</displayName>
                              <className>SomeTest</className>
                              <methodName>test1</methodName>
                          </entry>
                          <entry>
                              <fullEnclosingClassName>testcases.SomeTest</fullEnclosingClassName>
                              <displayName>Some cool test name 2</displayName>
                              <className>SomeTest</className>
                              <methodName>test2</methodName>
                          </entry>
                          <entry>
                              <fullEnclosingClassName>testcases.SomeTest</fullEnclosingClassName>
                              <displayName>Some cool test name 3</displayName>
                              <className>SomeTest</className>
                              <methodName>test3</methodName>
                          </entry>
                      </root>
                    """
            );
        }

        @Test
        void should_generate_correct_output_for_one_test_with_display_name_and_two_without() {
            expectTransformation(
                "testcases.SomeTest",
                """
                    package testcases;
                    
                    import org.junit.jupiter.api.Test;
                    import org.junit.jupiter.api.DisplayName;
                    
                    public class SomeTest {
                    
                        @Test
                        void test1() {
                        }
                    
                        @Test
                        @DisplayName("Some cool test name 2")
                        void test2() {
                        }
                    
                        @Test
                        void test3() {
                        }
                    }
                    """,
                """
                    <root>
                          <entry>
                              <fullEnclosingClassName>testcases.SomeTest</fullEnclosingClassName>
                              <displayName/>
                              <className>SomeTest</className>
                              <methodName>test1</methodName>
                          </entry>
                          <entry>
                              <fullEnclosingClassName>testcases.SomeTest</fullEnclosingClassName>
                              <displayName/>
                              <className>SomeTest</className>
                              <methodName>test3</methodName>
                          </entry>
                          <entry>
                              <fullEnclosingClassName>testcases.SomeTest</fullEnclosingClassName>
                              <displayName>Some cool test name 2</displayName>
                              <className>SomeTest</className>
                              <methodName>test2</methodName>
                          </entry>
                      </root>
                    """
            );
        }
    }

    void expectEmptyTransformation(String className, @Language("Java") String sources) {
        assertThatThrownBy(
            () -> expectTransformation(className, sources, null)
        ).hasMessageContaining("hasn't been called");
    }

    void expectTransformation(String className, @Language("Java") String sources, @Language("XML") String expectedOutput) {
        expectTransformation(Map.of(className, sources), expectedOutput);
    }

    void expectTransformation(Map<String, String> sources, @Language("XML") String expectedOutput) {
        var iterator = sources.entrySet().iterator();
        var first = iterator.next();
        var cute = this.cute.andSourceFile(first.getKey(), first.getValue());
        while (iterator.hasNext()) {
            var next = iterator.next();
            cute = cute.andSourceFile(next.getKey(), next.getValue());
        }
        cute.andUseCompilerOptions("-A" + TestKonvenceAP.indentXmlOption + "=true").whenCompiled()
            .thenExpectThat().compilationSucceeds()
            .andThat().fileObject(
                StandardLocation.SOURCE_OUTPUT,
                TestKonvenceAP.enforcementsXmlPackage,
                TestKonvenceAP.enforcementsXmlName
            ).matches(contentMatcher(expectedOutput))
            .executeTest();
    }

    private GeneratedFileObjectMatcher contentMatcher(String content) {
        return fileObject -> {
            try (var inputStream = fileObject.openInputStream()) {
                var actual = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                if (content == null) {
                    assertThat(actual).isEmpty();
                } else {
                    assertThat(actual).isNotBlank();
                    assertThat(
                        Arrays.stream(actual.split("\n")).map(String::trim).toList()
                    ).isEqualTo(Arrays.stream(content.split("\n")).map(String::trim).toList());
                }
            }
            return true;
        };
    }
}
