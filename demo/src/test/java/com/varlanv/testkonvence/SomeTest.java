package com.varlanv.testkonvence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SomeTest {

    @Test
    @DisplayName("one should equal to itself")
    void one_should_equal_to_itself() {
        assert 1 == 1;
    }

    @Test
    @DisplayName("two should equal to itself")
    void two_should_equal_to_itself() {
        assert 2 == 2;
    }
}
