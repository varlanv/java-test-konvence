package com.varlanv.testkonvence.proc;

import static org.assertj.core.api.Assertions.assertThat;

import com.varlanv.testkonvence.commontest.UnitTest;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class CamelMethodNameFromDisplayNameTest implements UnitTest {

    @TestFactory
    @DisplayName("default data table")
    Stream<DynamicTest> default_data_table() {
        return Stream.of(
                        Map.entry("", ""),
                        Map.entry("   ", ""),
                        Map.entry("1", ""),
                        Map.entry("12345", ""),
                        Map.entry("_123_", ""),
                        Map.entry("___", ""),
                        Map.entry("Some good test name", "someGoodTestName"),
                        Map.entry(
                                "Some good test name, which has comas, dots., question? marks, and exclamation marks!",
                                "someGoodTestNameWhichHasComasDotsQuestionMarksAndExclamationMarks"),
                        Map.entry("Some123good456test789name0", "some123good456test789name0"),
                        Map.entry("Слава Україні", ""),
                        Map.entry("qwe", "qwe"),
                        Map.entry("___abc___", "abc"),
                        Map.entry("a_b_c_", "aBC"),
                        Map.entry("a_b_c", "aBC"),
                        Map.entry("-a_b_c-", "aBC"),
                        Map.entry("a_b_c-", "aBC"),
                        Map.entry("-a_b_c", "aBC"),
                        Map.entry(
                                "Some good\n multiline   test\n name, which also has multiple whitespaces and_underscores   ",
                                "someGoodMultilineTestNameWhichAlsoHasMultipleWhitespacesAndUnderscores"))
                .map(entry -> DynamicTest.dynamicTest(
                        "Display name \"%s\" should be converted to camel method name \"%s\""
                                .formatted(entry.getKey(), entry.getValue()),
                        () -> {
                            var expectedMethodName = entry.getValue();
                            var displayName = entry.getKey();
                            var actualMethodName = CamelMethodNameFromDisplayName.convert(displayName);
                            assertThat(actualMethodName).isEqualTo(expectedMethodName);
                        }));
    }
}
