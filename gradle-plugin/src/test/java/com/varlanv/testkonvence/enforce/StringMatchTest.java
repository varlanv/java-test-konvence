package com.varlanv.testkonvence.enforce;

import com.varlanv.testkonvence.commontest.UnitTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringMatchTest implements UnitTest {

    @Nested
    class MatchingIndexes implements UnitTest {

        @Test
        void when_match_and_line_is_empty_then_return_empty() {
            assertThat(new StringMatch("", "").matchingIndexes().size()).isZero();
        }

        @Test
        void when_match_is_empty_then_return_empty() {
            assertThat(new StringMatch("qwerty", "").matchingIndexes().size()).isZero();
        }

        @Test
        void when_line_is_empty_then_return_empty() {
            assertThat(new StringMatch("", "asd").matchingIndexes().size()).isZero();
        }

        @Test
        void when_one_match_then_return_vector_with_one_index() {
            var actual = new StringMatch("asd", "a").matchingIndexes();

            assertThat(actual.size()).isEqualTo(1);
            assertThat(actual.first()).isEqualTo(0);
        }

        @Test
        void when_two_matches_then_return_vector_with_two_indexes() {
            var actual = new StringMatch("asa", "a").matchingIndexes();

            assertThat(actual.size()).isEqualTo(2);
            assertThat(actual.get(0)).isEqualTo(0);
            assertThat(actual.get(1)).isEqualTo(2);
        }

        @Test
        void when_no_matches_then_return_empty_vector() {
            var actual = new StringMatch("asa", "b").matchingIndexes();

            assertThat(actual.size()).isEqualTo(0);
        }

        @Test
        void when_full_match_then_return_vector_with_one_index() {
            var actual = new StringMatch("asa", "asa").matchingIndexes();

            assertThat(actual.size()).isEqualTo(1);
            assertThat(actual.first()).isEqualTo(0);
        }
    }
}
