package com.varlanv.jsonassured;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.stream.Stream;

class JsonAssuredTest {

    static Stream<String> test_happy() {
        return Stream.of("");
    }

    @MethodSource
    @ParameterizedTest
    void test_happy(String subject) {
    }

    @Nested
    class isTrue {

        @Test
        void when_val_is_false__then_fail() {
        }

        @Test
        void when_val_is_object__then_fail() {
        }

        @Test
        void when_val_is_0__then_fail() {
        }

        @Test
        void when_val_is_null__then_fail() {
        }
    }

    @Nested
    class isFalse {

        @Test
        void when_val_is_true__then_fail() {
        }

        @Test
        void when_val_is_object__then_fail() {
        }

        @Test
        void when_val_is_0__then_fail() {
        }

        @Test
        void when_val_is_null__then_fail() {
        }
    }

    @Nested
    class isNull {

        @Test
        void when_val_is_true__then_fail() {
        }

        @Test
        void when_val_is_object__then_fail() {
        }
    }

    @Nested
    class isEqual_string {

        @ParameterizedTest
        @ArgumentsSource(NullableBlankStrings.class)
        void when_blank_jsonPath__then_fail(String string) {
        }

        @Test
        void when_not_equal__then_fail() {
        }

        @Test
        void when_actual_is_null__then_fail() {
        }

        @Test
        void when_expected_is_null__then_fail() {
        }

        @Test
        void when_actual_is_number__then_fail() {
        }

        @Test
        void when_actual_is__object__then_fail() {
        }

        @Test
        void when_actual_is__array__then_fail() {
        }
    }

    @Nested
    class isNotNull {

        @Test
        void when_val_is_null__then_fail() {
        }

        @Test
        void when_val_is_not_null__then_ok() {
        }
    }

    @Nested
    class stringPath {

        @Test
        void when_int_type__then_fail() {
        }

        @Test
        void when_null_type__then_fail() {
        }

        @Test
        void when_string_array_type__then_fail() {
        }

        @Test
        void when_object_type__then_fail() {
        }

        @ParameterizedTest
        @ArgumentsSource(NullableBlankStrings.class)
        void isEqualTo__when_blank_jsonPath__then_fail(String blankString) {
        }

        @ParameterizedTest
        @ValueSource(strings = {"str", "STR", "qwe", "sTR"})
        void isEqualTo__when_not_equal__then_fail(String expected) {
        }

        @Test
        void isNotEqualTo__when_equal__then_fail() {
        }

        @Test
        void isEqualToIgnoringCase__when_not_equal__then_fail() {
        }

        @ParameterizedTest
        @ValueSource(strings = {"str", "STR", "sTR"})
        void isNotEqualToIgnoringCase__when_not_equal__then_fail(String expected) {
        }

        @ParameterizedTest
        @ArgumentsSource(NonNullBlankStrings.class)
        void isBlank__when_blank__then_ok(String blankString) {
        }

        @Test
        void isBlank__when_not_blank__then_fail() {
        }

        @ParameterizedTest
        @ArgumentsSource(NonNullBlankStrings.class)
        void isNotBlank__when_blank__then_fail(String blankString) {
        }

        @Test
        void isNotBlank__when_not_blank__then_ok() {
        }

        @Test
        void isEmpty__when_empty__then_ok() {
        }

        @Test
        void isEmpty__when_not_empty__then_fail() {
        }

        @Test
        void isNotEmpty__when_empty__then_fail() {
        }

        @Test
        void isNotEmpty__when_not_empty__then_ok() {
            ;
        }

        @Test
        void hasLength__when_not_matches__then_fail() {
        }

        @Test
        void hasLength__when_length_is_negative__then_fail() {
        }

        @Test
        void hasLength__when_length_matches__then_ok() {
        }

        @Test
        void hasLengthRange__when_range_matches__then_ok() {
        }

        @Test
        void hasLengthRange__when_min_length_is_negative__then_fail() {
        }

        @Test
        void hasLengthRange__when_max_length_is_negative__then_fail() {
        }

        @Test
        void hasLengthRange__when_max_less_than_min__then_fail() {
        }

        @Test
        void hasLengthRange__when__lower_mismatch__then_fail() {
        }

        @Test
        void hasLengthRange__when__upper_mismatch__then_fail() {
        }

        @Test
        void hasLengthAtLeast__when_min_length_is_negative__then_fail() {
        }

        @Test
        void hasLengthAtLeast__when_not_matches__then_fail() {
        }

        @Test
        void hasLengthAtMost__when_min_length_is_negative__then_fail() {
        }

        @Test
        void hasLengthAtMost__when_not_matches__then_fail() {
        }

        @Test
        void contains__when_not_contain__then_fail() {
        }

        @Test
        void containsIgnoringCase__when_not_contain__then_fail() {
        }

        @ParameterizedTest
        @ValueSource(strings = {"sTrr", ".qweq"})
        void matches__when_not_matches__then_fail(String pattern) {
        }

        @ParameterizedTest
        @ValueSource(strings = {"sTr", ".*"})
        void doesNotMatch__when_matches__then_fail(String pattern) {
        }

        @Test
        void isIn__when_not_in__then_fail() {
        }

        @Test
        void isIn__when_charSequences_in__then_ok() {
        }

        @Test
        void isIn_when_input_contains_null__then_fail() {
        }

        @Test
        void isNotIn__when_in__then_fail() {
        }

        @Test
        void satisfies_when_null_input__then_fail() {
        }

        @Test
        void satisfies__when_not_satisfy_then_fail() {
        }
    }

    @Nested
    class doesNotExist {

        @ParameterizedTest
        @ArgumentsSource(NullableBlankStrings.class)
        void when_null_jsonPath__then_fail(String string) {
        }

        @Test
        void when_not_exists__then_ok() {
        }

        @Test
        void when_null_path__then_fail() {
        }

        @Test
        void when_exists_and_null__then_fail() {
        }

        @Test
        void when_exists_and_object__then_fail() {
        }

        @Test
        void when_exists_and_0__then_fail() {
        }
    }

    static class NullableBlankStrings implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext)
            throws Exception {
            return Stream.concat(NonNullBlankStrings.strings(), Stream.builder().add(null).build())
                .map(Arguments::of);
        }
    }

    @Nested
    class intPath {

        @Test
        void isNegative__when_string_value__then_fail() {
        }

        @Test
        void isNegative__when_null__then_fail() {
        }

        @Test
        void isNegative__when_array__then_fail() {
        }

        @Test
        void isNegative__when_object__then_fail() {
        }

        @Test
        void isNegative__when_decimal__then_fail() {
        }

        @Test
        void isNegative__when_long__then_fail() {
        }

        @Test
        void isNegative__when_small_decimal__then_fail() {
        }

        @Test
        void isNegative__when_boolean__then_fail() {
        }

        @Test
        void isNegative__when_zero_decimal__then_fail() {
        }

        @Test
        void isNegative__when_int_as_string__then_fail() {
        }

        @Test
        void isNegative__when_positive__then_fail() {
        }

        @Test
        void isNegative__when_zero__then_fail() {
        }

        @Test
        void isPositive__when_negative__then_fail() {
        }

        @Test
        void isPositive__when_zero__then_fail() {
        }

        @Test
        void isZero__when_positive__then_fail() {
        }

        @Test
        void isZero__when_negative__then_fail() {
        }

        @Test
        void isEqualTo__when_equal__then_fail() {
        }

        @Test
        void isEqualTo_when_null_input__then_fail() {
        }

        @Test
        void isNotEqualTo_when_null_input__then_fail() {
        }

        @Test
        void isNotEqualTo_when_equal_input__then_fail() {
        }

        @Test
        void isGte_when_null_input__then_fail() {
        }

        @Test
        void isGte_when_not_gte__then_fail() {
        }

        @Test
        void isLte_when_null_input__then_fail() {
        }

        @Test
        void isLte_when_not_lte__then_fail() {
        }

        @Test
        void isInRange_when_null_min__then_fail() {
        }

        @Test
        void isInRange_when_null_max__then_fail() {
        }

        @Test
        void isInRange_when_bottom_higher_than_top__then_fail() {
        }

        @Test
        void isInRange_when_not_in_range_bottom__then_fail() {
        }

        @Test
        void isInRange_when_not_in_range_top__then_fail() {
        }

        @Test
        void isIn_when_null_input__then_fail() {
        }

        @Test
        void isIn_when_input_contains_null__then_fail() {
        }

        @Test
        void isIn_when_not_in__then_fail() {
        }

        @Test
        void isNotIn_when_null_input__then_fail() {
        }

        @Test
        void isNotIn_when__in__then_fail() {
        }

        @Test
        void satisfies_when_null_input__then_fail() {
        }

        @Test
        void satisfies_when_not_satisfies__then_fail() {
        }
    }

    @Nested
    class longPath {

        @Test
        void isNegative__when_string_value__then_fail() {
        }

        @Test
        void isNegative__when_null__then_fail() {
        }

        @Test
        void isNegative__when_array__then_fail() {
        }

        @Test
        void isNegative__when_object__then_fail() {
        }

        @Test
        void isNegative__when_decimal__then_fail() {
        }

        @Test
        void isNegative__when_int__then_ok() {
        }

        @Test
        void isNegative__when_small_decimal__then_fail() {
        }

        @Test
        void isNegative__when_boolean__then_fail() {
        }

        @Test
        void isNegative__when_zero_decimal__then_fail() {
        }

        @Test
        void isNegative__when_int_as_string__then_fail() {
        }

        @Test
        void isNegative__when_positive__then_fail() {
        }
    }

    @Nested
    class decimalPath {

        @Test
        void isNegative__when_string_value__then_fail() {
        }

        @Test
        void isNegative__when_null__then_fail() {
        }

        @Test
        void isNegative__when_array__then_fail() {
        }

        @Test
        void isNegative__when_object__then_fail() {
        }

        @Test
        void isNegative__when_long__then_fail() {
        }

        @Test
        void isNegative__when_int_then_fail() {
        }

        @Test
        void isNegative__when_boolean__then_fail() {
        }

        @Test
        void isNegative__when_zero_decimal__then_fail() {
        }

        @Test
        void isNegative__when_int_as_string__then_fail() {
        }

        @Test
        void isNegative__when_positive__then_fail() {
        }
    }

    @Nested
    class stringArrayPath {

        @Test
        void when_null_target_path__then_fail() {
        }

        @Test
        void when_string_target_path__then_fail() {
        }

        @Test
        void when_object_array_target_path__then_fail() {
        }

        @Test
        void when_number_array_target_path__then_fail() {
        }

        @Test
        void when_only_nulls__array_target_path__then_ok() {
        }

        @Test
        void when_nulls_and_strings__array_target_path__then_ok() {
        }

        @Test
        void when_empty__array_target_path__then_ok() {
        }
    }

    static class NonNullBlankStrings implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext)
            throws Exception {
            return strings().map(Arguments::of);
        }

        static Stream<String> strings() {
            return Stream.of("", "  ", "\t", "\n");
        }
    }
}
