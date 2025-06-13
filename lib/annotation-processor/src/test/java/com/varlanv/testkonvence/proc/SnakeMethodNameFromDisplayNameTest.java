package com.varlanv.testkonvence.proc;

import static org.assertj.core.api.Assertions.assertThat;

import com.varlanv.testkonvence.commontest.UnitTest;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;

class SnakeMethodNameFromDisplayNameTest implements UnitTest {

    private long totalTime;

    @BeforeAll
    void beforeAll() {
        totalTime = System.nanoTime();
    }

    @AfterAll
    void afterAll() {
        String msg = "TOTAL -> " + Duration.ofNanos(System.nanoTime() - totalTime);
        System.err.println(msg);
        System.err.println(msg);
        System.err.println(msg);
    }

    @TestFactory
    @DisplayName("default data table")
    Stream<DynamicTest> default_data_table() {
        return Stream.of(
                        // Empty strings and whitespace
                        Map.entry("", ""),
                        Map.entry("   ", ""),
                        // Numbers only
                        Map.entry("1", ""),
                        Map.entry("12345", ""),
                        Map.entry("_123_", ""),
                        // Special characters only
                        Map.entry("___", ""),
                        Map.entry("!@#$%^&*()", ""),
                        Map.entry("...", ""),
                        Map.entry("---", ""),
                        // Basic conversions
                        Map.entry("Some good test name", "some_good_test_name"),
                        Map.entry(
                                "Some good test name, which has comas, dots., question? marks, and exclamation marks!",
                                "some_good_test_name_which_has_comas_dots_question_marks_and_exclamation_marks"),
                        // Numbers in different positions
                        Map.entry("Some123good456test789name0", "some123good456test789name0"),
                        Map.entry("123Some good test", "some_good_test"),
                        Map.entry("Some good test123", "some_good_test123"),
                        Map.entry("123Some456good789test", "some456good789test"),
                        // Non-ASCII characters
                        Map.entry("Слава Україні", ""),
                        Map.entry("café", "caf"),
                        Map.entry("über test", "ber_test"),
                        // Short strings
                        Map.entry("qwe", "qwe"),
                        Map.entry("a", "a"),
                        Map.entry("A", "a"),
                        // Underscores in different positions
                        Map.entry("___abc___", "abc"),
                        Map.entry("a_b_c_", "a_b_c"),
                        Map.entry("a_b_c", "a_b_c"),
                        Map.entry("_a_b_c_d_", "a_b_c_d"),
                        // Hyphens and other special characters
                        Map.entry("-a_b_c-", "a_b_c"),
                        Map.entry("a_b_c-", "a_b_c"),
                        Map.entry("-a_b_c", "a_b_c"),
                        Map.entry("a-b-c", "a_b_c"),
                        Map.entry("a.b.c", "a_b_c"),
                        Map.entry("a:b:c", "a_b_c"),
                        // Mixed case
                        Map.entry("CamelCaseTest", "camelcasetest"),
                        Map.entry("snake_case_test", "snake_case_test"),
                        Map.entry("UPPER_CASE_TEST", "upper_case_test"),
                        Map.entry("MixedCase_snake_UPPER", "mixedcase_snake_upper"),
                        // Multiline and whitespace
                        Map.entry(
                                "Some good\n multiline   test\n name, which also has multiple whitespaces and_underscores   ",
                                "some_good_multiline_test_name_which_also_has_multiple_whitespaces_and_underscores"),
                        Map.entry("  Spaces  at  various  places  ", "spaces_at_various_places"),
                        // Complex combinations
                        Map.entry("123_Test-With.Various:Special@Characters!", "test_with_various_special_characters"),
                        Map.entry("___Multiple___Consecutive___Separators___", "multiple_consecutive_separators"),
                        Map.entry("1_2_3_a_b_c", "2_3_a_b_c"),
                        Map.entry("a_1_b_2_c_3", "a_1_b_2_c_3"))
                .map(entry -> DynamicTest.dynamicTest(
                        "Display name \"%s\" should be converted to method name \"%s\""
                                .formatted(entry.getKey(), entry.getValue()),
                        () -> {
                            var expectedMethodName = entry.getValue();
                            var displayName = entry.getKey();
                            var actualMethodName = SnakeMethodNameFromDisplayName.convert(displayName);
                            assertThat(actualMethodName).isEqualTo(expectedMethodName);
                        }));
    }
}
