package com.varlanv.testkonvence.proc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.varlanv.testkonvence.Constants;
import com.varlanv.testkonvence.commontest.UnitTest;
import io.toolisticon.cute.Cute;
import io.toolisticon.cute.CuteApi;
import io.toolisticon.cute.GeneratedFileObjectMatcher;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.tools.StandardLocation;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TestKonvenceAPTest implements UnitTest {

    CuteApi.BlackBoxTestSourceFilesInterface cute = Cute.blackBoxTest().given().processor(TestKonvenceAP.class);

    @Nested
    class EmptyOutputFile implements UnitTest {

        @Test
        @DisplayName("when doesnt find annotation then write empty file")
        void when_doesnt_find_annotation__then_write_empty_file() {
            expectEmptyTransformation(
                    "testcases.SomeTest",
                    """
                    package testcases;

                    public class SomeTest {

                        void test() {
                        }
                    }
                    """);
        }
    }

    @Nested
    class SingleClassFileWithSingleTestMethod implements UnitTest {

        @Test
        @DisplayName("when display name annotation first then should generate output")
        void when_display_name_annotation_first_then_should_generate_output() {
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
                            <q>
                                <w>
                                    <e>
                                        <o>testcases.SomeTest</o>
                                        <r>
                                            <t>
                                                <i>SomeTest</i>
                                                <y>
                                                    <u>
                                                        <p>Some cool test name</p>
                                                        <a>test</a>
                                                        <s>some_cool_test_name</s>
                                                    </u>
                                                </y>
                                            </t>
                                        </r>
                                    </e>
                                </w>
                            </q>
                        </root>""");
        }

        @Test
        @DisplayName("when test annotation first then should generate output")
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
                            <q>
                                <w>
                                    <e>
                                        <o>testcases.SomeTest</o>
                                        <r>
                                            <t>
                                                <i>SomeTest</i>
                                                <y>
                                                    <u>
                                                        <p>Some cool test name</p>
                                                        <a>test</a>
                                                        <s>some_cool_test_name</s>
                                                    </u>
                                                </y>
                                            </t>
                                        </r>
                                    </e>
                                </w>
                            </q>
                        </root>""");
        }

        @Test
        @DisplayName("when no display name annotation then should generate output")
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
                            <q>
                                <w>
                                    <e>
                                        <o>testcases.SomeTest</o>
                                        <r>
                                            <t>
                                                <i>SomeTest</i>
                                                <y>
                                                    <u>
                                                        <p>test</p>
                                                        <a>test</a>
                                                        <s/>
                                                    </u>
                                                </y>
                                            </t>
                                        </r>
                                    </e>
                                </w>
                            </q>
                        </root>""");
        }

        @Test
        @DisplayName("when display name is on class level then should generate output")
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
                            <q>
                                <w>
                                    <e>
                                        <o>testcases.SomeTest</o>
                                        <r>
                                            <t>
                                                <i>SomeTest</i>
                                                <y>
                                                    <u>
                                                        <p>test</p>
                                                        <a>test</a>
                                                        <s/>
                                                    </u>
                                                </y>
                                            </t>
                                        </r>
                                    </e>
                                </w>
                            </q>
                        </root>""");
        }

        @Test
        @DisplayName("when display name is on parameterized test then should generate output")
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
                        <q>
                            <w>
                                <e>
                                    <o>testcases.SomeTest</o>
                                    <r>
                                        <t>
                                            <i>SomeTest</i>
                                            <y>
                                                <u>
                                                    <p>test</p>
                                                    <a>test</a>
                                                    <s/>
                                                </u>
                                            </y>
                                        </t>
                                    </r>
                                </e>
                            </w>
                        </q>
                    </root>""");
        }
    }

    @Nested
    class SingleClassFileWithThreeTestMethods implements UnitTest {

        @Test
        @DisplayName("should generate correct output for three test with display name")
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
                            <q>
                                <w>
                                    <e>
                                        <o>testcases.SomeTest</o>
                                        <r>
                                            <t>
                                                <i>SomeTest</i>
                                                <y>
                                                    <u>
                                                        <p>Some cool test name 1</p>
                                                        <a>test1</a>
                                                        <s>some_cool_test_name_1</s>
                                                    </u>
                                                    <u>
                                                        <p>Some cool test name 2</p>
                                                        <a>test2</a>
                                                        <s>some_cool_test_name_2</s>
                                                    </u>
                                                    <u>
                                                        <p>Some cool test name 3</p>
                                                        <a>test3</a>
                                                        <s>some_cool_test_name_3</s>
                                                    </u>
                                                </y>
                                            </t>
                                        </r>
                                    </e>
                                </w>
                            </q>
                        </root>""");
        }

        @Test
        @DisplayName("should generate correct output for one test with display name and two without")
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
                            <q>
                                <w>
                                    <e>
                                        <o>testcases.SomeTest</o>
                                        <r>
                                            <t>
                                                <i>SomeTest</i>
                                                <y>
                                                    <u>
                                                        <p>test1</p>
                                                        <a>test1</a>
                                                        <s/>
                                                    </u>
                                                    <u>
                                                        <p>Some cool test name 2</p>
                                                        <a>test2</a>
                                                        <s>some_cool_test_name_2</s>
                                                    </u>
                                                    <u>
                                                        <p>test3</p>
                                                        <a>test3</a>
                                                        <s/>
                                                    </u>
                                                </y>
                                            </t>
                                        </r>
                                    </e>
                                </w>
                            </q>
                        </root>""");
        }
    }

    void expectEmptyTransformation(String className, @Language("Java") String sources) {
        assertThatThrownBy(() -> expectTransformation(className, sources, null))
                .hasMessageContaining("hasn't been called");
    }

    void expectTransformation(
            String className, @Language("Java") String sources, @Language("XML") @Nullable String expectedOutput) {
        expectTransformation(Map.of(className, sources), expectedOutput);
    }

    void expectTransformation(Map<String, String> sources, @Language("XML") @Nullable String expectedOutput) {
        var iterator = sources.entrySet().iterator();
        var first = iterator.next();
        var cute = this.cute.andSourceFile(first.getKey(), first.getValue());
        while (iterator.hasNext()) {
            var next = iterator.next();
            cute = cute.andSourceFile(next.getKey(), next.getValue());
        }
        cute.andUseCompilerOptions("-A" + Constants.apIndentXmlOption + "=true")
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .fileObject(
                        StandardLocation.SOURCE_OUTPUT,
                        Constants.apEnforcementsXmlPackage,
                        Constants.apEnforcementsXmlName)
                .matches(contentMatcher(expectedOutput))
                .executeTest();
    }

    private GeneratedFileObjectMatcher contentMatcher(@Nullable String content) {
        return fileObject -> {
            try (var inputStream = fileObject.openInputStream()) {
                var actual = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                if (content == null) {
                    assertThat(actual).isEmpty();
                } else {
                    assertThat(actual).isNotBlank();
                    var actualString =
                            Arrays.stream(actual.split("\n")).map(String::trim).collect(Collectors.joining());
                    var expectedString =
                            Arrays.stream(content.split("\n")).map(String::trim).collect(Collectors.joining());
                    assertThat(actualString).isEqualTo(expectedString);
                }
            }
            return true;
        };
    }
}
