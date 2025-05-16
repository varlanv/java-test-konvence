package com.varlanv.testkonvence.gradle.plugin;

import static org.assertj.core.api.Assertions.*;

import com.varlanv.testkonvence.commontest.UnitTest;
import java.util.ArrayList;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IntVectorTest implements UnitTest {

    @Test
    void should_throw_exception_on_negative_initial_capacity() {
        assertThatThrownBy(() -> new IntVector(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Nested
    class First implements UnitTest {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 10})
        void should_throw_exception_on_empty_vector(int initialCapacity) {
            var intVector = new IntVector(initialCapacity);
            assertThatThrownBy(intVector::first).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 10})
        void should_return_first_element_for_vector_of_one_element(int initialCapacity) {
            var subject = new IntVector(initialCapacity).add(5);

            assertThat(subject.first()).isEqualTo(5);
        }

        @ParameterizedTest
        @ValueSource(ints = {2, 5, 10})
        void should_return_first_element_for_vector_of_many_elements_when_initial_capacity_was_zero(int elementsCount) {
            var subject = IntStream.range(0, elementsCount)
                    .map(idx -> idx + 1)
                    .boxed()
                    .reduce(new IntVector(0), IntVector::add, (a, b) -> b);

            assertThat(subject.first()).isEqualTo(1);
        }

        @ParameterizedTest
        @ValueSource(ints = {2, 5, 10})
        void should_return_first_element_for_vector_of_many_elements_when_initial_capacity_was_non_zero(
                int elementsCount) {
            var subject = IntStream.range(0, elementsCount)
                    .map(idx -> idx + 1)
                    .boxed()
                    .reduce(new IntVector(50), IntVector::add, (a, b) -> b);

            assertThat(subject.first()).isEqualTo(1);
        }
    }

    @Nested
    class Size implements UnitTest {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 10})
        void should_return_0_for_empty_vector(int initialCapacity) {
            assertThat(new IntVector(initialCapacity).size()).isEqualTo(0);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 10, 100})
        void should_return_count_of_elements_for_non_empty_vector(int elementsCount) {
            var actual =
                    IntStream.range(0, elementsCount).boxed().reduce(new IntVector(0), IntVector::add, (a, b) -> b);

            assertThat(actual.size()).isEqualTo(elementsCount);
        }
    }

    @Nested
    class ForEach implements UnitTest {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 10})
        void should_not_iterate_over_empty_vector(int initialCapacity) {
            assertThatNoException().isThrownBy(() -> new IntVector(initialCapacity).forEach(line -> {
                throw new RuntimeException("Should not reach here");
            }));
        }

        @ValueSource(ints = {1, 3, 10})
        @ParameterizedTest
        void should_iterate_over_filled_vector(int initialCapacity) {
            var subject = new IntVector(initialCapacity);
            for (int i = 0; i < initialCapacity; i++) {
                subject.add(i);
            }

            var result = new ArrayList<Integer>();
            subject.forEach(result::add);
            assertThat(result).hasSize(initialCapacity);
            assertThat(result)
                    .containsExactlyElementsOf(
                            IntStream.range(0, initialCapacity).boxed().toList());
        }

        @Test
        void should_increase_capacity_and_still_hold_previous_elements() {
            var actual = new ArrayList<Integer>();
            new IntVector(0).add(1).add(2).add(3).forEach(actual::add);
            assertThat(actual).containsExactly(1, 2, 3);
        }
    }
}
