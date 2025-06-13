package com.varlanv.jsonassured;

import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("test happy")
    void test_happy(String subject) {
    }

    @Nested
    class isTrue {

        @Test
        @DisplayName("when val is false then fail")
        void when_val_is_false__then_fail() {
        }

        @Test
        @DisplayName("when val is object then fail")
        void when_val_is_object__then_fail() {
        }

        @Test
        @DisplayName("when val is 0 then fail")
        void when_val_is_0__then_fail() {
        }

        @Test
        @DisplayName("when val is null then fail")
        void when_val_is_null__then_fail() {
        }
    }

    @Nested
    class isFalse {

        @Test
        @DisplayName("when val is true then fail")
        void when_val_is_true__then_fail() {
        }

        @Test
        @DisplayName("when val is object then fail")
        void when_val_is_object__then_fail() {
        }

        @Test
        @DisplayName("when val is 0 then fail")
        void when_val_is_0__then_fail() {
        }

        @Test
        @DisplayName("when val is null then fail")
        void when_val_is_null__then_fail() {
        }
    }

    @Nested
    class isNull {

        @Test
        @DisplayName("when val is true then fail")
        void when_val_is_true__then_fail() {
        }

        @Test
        @DisplayName("when val is object then fail")
        void when_val_is_object__then_fail() {
        }
    }

    @Nested
    class isEqual_string {

        @ParameterizedTest
        @ArgumentsSource(NullableBlankStrings.class)
        @DisplayName("when blank 'jsonPath' then fail")
        void when_blank_jsonPath__then_fail(String string) {
        }

        @Test
        @DisplayName("when not equal then fail")
        void when_not_equal__then_fail() {
        }

        @Test
        @DisplayName("when actual is null then fail")
        void when_actual_is_null__then_fail() {
        }

        @Test
        @DisplayName("when expected is null then fail")
        void when_expected_is_null__then_fail() {
        }

        @Test
        @DisplayName("when actual is number then fail")
        void when_actual_is_number__then_fail() {
        }

        @Test
        @DisplayName("when actual is object then fail")
        void when_actual_is__object__then_fail() {
        }

        @Test
        @DisplayName("when actual is array then fail")
        void when_actual_is__array__then_fail() {
        }
    }

    @Nested
    class isNotNull {

        @Test
        @DisplayName("when val is null then fail")
        void when_val_is_null__then_fail() {
        }

        @Test
        @DisplayName("when val is not null then ok")
        void when_val_is_not_null__then_ok() {
        }
    }

    @Nested
    class stringPath {

        @Test
        @DisplayName("when int type then fail")
        void when_int_type__then_fail() {
        }

        @Test
        @DisplayName("when null type then fail")
        void when_null_type__then_fail() {
        }

        @Test
        @DisplayName("when string array type then fail")
        void when_string_array_type__then_fail() {
        }

        @Test
        @DisplayName("when object type then fail")
        void when_object_type__then_fail() {
        }

        @ParameterizedTest
        @ArgumentsSource(NullableBlankStrings.class)
        @DisplayName("'isEqualTo' when blank 'jsonPath' then fail")
        void isEqualTo__when_blank_jsonPath__then_fail(String blankString) {
        }

        @ParameterizedTest
        @ValueSource(strings = {"str", "STR", "qwe", "sTR"})
        @DisplayName("'isEqualTo' when not equal then fail")
        void isEqualTo__when_not_equal__then_fail(String expected) {
        }

        @Test
        @DisplayName("'isNotEqualTo' when equal then fail")
        void isNotEqualTo__when_equal__then_fail() {
        }

        @Test
        @DisplayName("'isEqualToIgnoringCase' when not equal then fail")
        void isEqualToIgnoringCase__when_not_equal__then_fail() {
        }

        @ParameterizedTest
        @ValueSource(strings = {"str", "STR", "sTR"})
        @DisplayName("'isNotEqualToIgnoringCase' when not equal then fail")
        void isNotEqualToIgnoringCase__when_not_equal__then_fail(String expected) {
        }

        @ParameterizedTest
        @ArgumentsSource(NonNullBlankStrings.class)
        @DisplayName("'isBlank' when blank then ok")
        void isBlank__when_blank__then_ok(String blankString) {
        }

        @Test
        @DisplayName("'isBlank' when not blank then fail")
        void isBlank__when_not_blank__then_fail() {
        }

        @ParameterizedTest
        @ArgumentsSource(NonNullBlankStrings.class)
        @DisplayName("'isNotBlank' when blank then fail")
        void isNotBlank__when_blank__then_fail(String blankString) {
        }

        @Test
        @DisplayName("'isNotBlank' when not blank then ok")
        void isNotBlank__when_not_blank__then_ok() {
        }

        @Test
        @DisplayName("'isEmpty' when empty then ok")
        void isEmpty__when_empty__then_ok() {
        }

        @Test
        @DisplayName("'isEmpty' when not empty then fail")
        void isEmpty__when_not_empty__then_fail() {
        }

        @Test
        @DisplayName("'isNotEmpty' when empty then fail")
        void isNotEmpty__when_empty__then_fail() {
        }

        @Test
        @DisplayName("'isNotEmpty' when not empty then ok")
        void isNotEmpty__when_not_empty__then_ok() {
            ;
        }

        @Test
        @DisplayName("'hasLength' when not matches then fail")
        void hasLength__when_not_matches__then_fail() {
        }

        @Test
        @DisplayName("'hasLength' when length is negative then fail")
        void hasLength__when_length_is_negative__then_fail() {
        }

        @Test
        @DisplayName("'hasLength' when length matches then ok")
        void hasLength__when_length_matches__then_ok() {
        }

        @Test
        @DisplayName("'hasLengthRange' when range matches then ok")
        void hasLengthRange__when_range_matches__then_ok() {
        }

        @Test
        @DisplayName("'hasLengthRange' when min length is negative then fail")
        void hasLengthRange__when_min_length_is_negative__then_fail() {
        }

        @Test
        @DisplayName("'hasLengthRange' when max length is negative then fail")
        void hasLengthRange__when_max_length_is_negative__then_fail() {
        }

        @Test
        @DisplayName("'hasLengthRange' when max less than min then fail")
        void hasLengthRange__when_max_less_than_min__then_fail() {
        }

        @Test
        @DisplayName("'hasLengthRange' when lower mismatch then fail")
        void hasLengthRange__when__lower_mismatch__then_fail() {
        }

        @Test
        @DisplayName("'hasLengthRange' when upper mismatch then fail")
        void hasLengthRange__when__upper_mismatch__then_fail() {
        }

        @Test
        @DisplayName("'hasLengthAtLeast' when min length is negative then fail")
        void hasLengthAtLeast__when_min_length_is_negative__then_fail() {
        }

        @Test
        @DisplayName("'hasLengthAtLeast' when not matches then fail")
        void hasLengthAtLeast__when_not_matches__then_fail() {
        }

        @Test
        @DisplayName("'hasLengthAtMost' when min length is negative then fail")
        void hasLengthAtMost__when_min_length_is_negative__then_fail() {
        }

        @Test
        @DisplayName("'hasLengthAtMost' when not matches then fail")
        void hasLengthAtMost__when_not_matches__then_fail() {
        }

        @Test
        @DisplayName("contains when not contain then fail")
        void contains__when_not_contain__then_fail() {
        }

        @Test
        @DisplayName("'containsIgnoringCase' when not contain then fail")
        void containsIgnoringCase__when_not_contain__then_fail() {
        }

        @ParameterizedTest
        @ValueSource(strings = {"sTrr", ".qweq"})
        @DisplayName("matches when not matches then fail")
        void matches__when_not_matches__then_fail(String pattern) {
        }

        @ParameterizedTest
        @ValueSource(strings = {"sTr", ".*"})
        @DisplayName("'doesNotMatch' when matches then fail")
        void doesNotMatch__when_matches__then_fail(String pattern) {
        }

        @Test
        @DisplayName("'isIn' when not in then fail")
        void isIn__when_not_in__then_fail() {
        }

        @Test
        @DisplayName("'isIn' when 'charSequences' in then ok")
        void isIn__when_charSequences_in__then_ok() {
        }

        @Test
        @DisplayName("'isIn' when input contains null then fail")
        void isIn_when_input_contains_null__then_fail() {
        }

        @Test
        @DisplayName("'isNotIn' when in then fail")
        void isNotIn__when_in__then_fail() {
        }

        @Test
        @DisplayName("satisfies when null input then fail")
        void satisfies_when_null_input__then_fail() {
        }

        @Test
        @DisplayName("satisfies when not satisfy then fail")
        void satisfies__when_not_satisfy_then_fail() {
        }
    }

    @Nested
    class doesNotExist {

        @ParameterizedTest
        @ArgumentsSource(NullableBlankStrings.class)
        @DisplayName("when null 'jsonPath' then fail")
        void when_null_jsonPath__then_fail(String string) {
        }

        @Test
        @DisplayName("when not exists then ok")
        void when_not_exists__then_ok() {
        }

        @Test
        @DisplayName("when null path then fail")
        void when_null_path__then_fail() {
        }

        @Test
        @DisplayName("when exists and null then fail")
        void when_exists_and_null__then_fail() {
        }

        @Test
        @DisplayName("when exists and object then fail")
        void when_exists_and_object__then_fail() {
        }

        @Test
        @DisplayName("when exists and 0 then fail")
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
        @DisplayName("'isNegative' when string value then fail")
        void isNegative__when_string_value__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when null then fail")
        void isNegative__when_null__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when array then fail")
        void isNegative__when_array__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when object then fail")
        void isNegative__when_object__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when decimal then fail")
        void isNegative__when_decimal__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when long then fail")
        void isNegative__when_long__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when small decimal then fail")
        void isNegative__when_small_decimal__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when boolean then fail")
        void isNegative__when_boolean__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when zero decimal then fail")
        void isNegative__when_zero_decimal__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when int as string then fail")
        void isNegative__when_int_as_string__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when positive then fail")
        void isNegative__when_positive__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when zero then fail")
        void isNegative__when_zero__then_fail() {
        }

        @Test
        @DisplayName("'isPositive' when negative then fail")
        void isPositive__when_negative__then_fail() {
        }

        @Test
        @DisplayName("'isPositive' when zero then fail")
        void isPositive__when_zero__then_fail() {
        }

        @Test
        @DisplayName("'isZero' when positive then fail")
        void isZero__when_positive__then_fail() {
        }

        @Test
        @DisplayName("'isZero' when negative then fail")
        void isZero__when_negative__then_fail() {
        }

        @Test
        @DisplayName("'isEqualTo' when equal then fail")
        void isEqualTo__when_equal__then_fail() {
        }

        @Test
        @DisplayName("'isEqualTo' when null input then fail")
        void isEqualTo_when_null_input__then_fail() {
        }

        @Test
        @DisplayName("'isNotEqualTo' when null input then fail")
        void isNotEqualTo_when_null_input__then_fail() {
        }

        @Test
        @DisplayName("'isNotEqualTo' when equal input then fail")
        void isNotEqualTo_when_equal_input__then_fail() {
        }

        @Test
        @DisplayName("'isGte' when null input then fail")
        void isGte_when_null_input__then_fail() {
        }

        @Test
        @DisplayName("'isGte' when not gte then fail")
        void isGte_when_not_gte__then_fail() {
        }

        @Test
        @DisplayName("'isLte' when null input then fail")
        void isLte_when_null_input__then_fail() {
        }

        @Test
        @DisplayName("'isLte' when not lte then fail")
        void isLte_when_not_lte__then_fail() {
        }

        @Test
        @DisplayName("'isInRange' when null min then fail")
        void isInRange_when_null_min__then_fail() {
        }

        @Test
        @DisplayName("'isInRange' when null max then fail")
        void isInRange_when_null_max__then_fail() {
        }

        @Test
        @DisplayName("'isInRange' when bottom higher than top then fail")
        void isInRange_when_bottom_higher_than_top__then_fail() {
        }

        @Test
        @DisplayName("'isInRange' when not in range bottom then fail")
        void isInRange_when_not_in_range_bottom__then_fail() {
        }

        @Test
        @DisplayName("'isInRange' when not in range top then fail")
        void isInRange_when_not_in_range_top__then_fail() {
        }

        @Test
        @DisplayName("'isIn' when null input then fail")
        void isIn_when_null_input__then_fail() {
        }

        @Test
        @DisplayName("'isIn' when input contains null then fail")
        void isIn_when_input_contains_null__then_fail() {
        }

        @Test
        @DisplayName("'isIn' when not in then fail")
        void isIn_when_not_in__then_fail() {
        }

        @Test
        @DisplayName("'isNotIn' when null input then fail")
        void isNotIn_when_null_input__then_fail() {
        }

        @Test
        @DisplayName("'isNotIn' when in then fail")
        void isNotIn_when__in__then_fail() {
        }

        @Test
        @DisplayName("satisfies when null input then fail")
        void satisfies_when_null_input__then_fail() {
        }

        @Test
        @DisplayName("satisfies when not satisfies then fail")
        void satisfies_when_not_satisfies__then_fail() {
        }
    }

    @Nested
    class longPath {

        @Test
        @DisplayName("'isNegative' when string value then fail")
        void isNegative__when_string_value__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when null then fail")
        void isNegative__when_null__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when array then fail")
        void isNegative__when_array__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when object then fail")
        void isNegative__when_object__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when decimal then fail")
        void isNegative__when_decimal__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when int then ok")
        void isNegative__when_int__then_ok() {
        }

        @Test
        @DisplayName("'isNegative' when small decimal then fail")
        void isNegative__when_small_decimal__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when boolean then fail")
        void isNegative__when_boolean__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when zero decimal then fail")
        void isNegative__when_zero_decimal__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when int as string then fail")
        void isNegative__when_int_as_string__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when positive then fail")
        void isNegative__when_positive__then_fail() {
        }
    }

    @Nested
    class decimalPath {

        @Test
        @DisplayName("'isNegative' when string value then fail")
        void isNegative__when_string_value__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when null then fail")
        void isNegative__when_null__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when array then fail")
        void isNegative__when_array__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when object then fail")
        void isNegative__when_object__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when long then fail")
        void isNegative__when_long__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when int then fail")
        void isNegative__when_int_then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when boolean then fail")
        void isNegative__when_boolean__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when zero decimal then fail")
        void isNegative__when_zero_decimal__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when int as string then fail")
        void isNegative__when_int_as_string__then_fail() {
        }

        @Test
        @DisplayName("'isNegative' when positive then fail")
        void isNegative__when_positive__then_fail() {
        }
    }

    @Nested
    class stringArrayPath {

        @Test
        @DisplayName("when null target path then fail")
        void when_null_target_path__then_fail() {
        }

        @Test
        @DisplayName("when string target path then fail")
        void when_string_target_path__then_fail() {
        }

        @Test
        @DisplayName("when object array target path then fail")
        void when_object_array_target_path__then_fail() {
        }

        @Test
        @DisplayName("when number array target path then fail")
        void when_number_array_target_path__then_fail() {
        }

        @Test
        @DisplayName("when only nulls array target path then ok")
        void when_only_nulls__array_target_path__then_ok() {
        }

        @Test
        @DisplayName("when nulls and strings array target path then ok")
        void when_nulls_and_strings__array_target_path__then_ok() {
        }

        @Test
        @DisplayName("when empty array target path then ok")
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
