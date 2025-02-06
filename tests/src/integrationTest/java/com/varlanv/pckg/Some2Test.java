package com.varlanv.pckg;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Outer nest")
public class Some2Test {

    @Test
    @DisplayName("Outer nest test")
    void outer_nest_test() {
        System.out.println("outer_nest_test");
    }

    @Nested
    @DisplayName("One Level Nest")
    class OneLevelNest {

        @Test
        @DisplayName("One level nest ..>-_-';=    test 123")
        void one_level_nest_test() {
            System.out.println("one_level_nest_test");
        }

        @Nested
        @DisplayName("Two Level Nest")
        class TwoLevelNest {

            @Test
            @DisplayName("Two level nest test")
            void two_level_nest_test() {
                System.out.println("two_level_nest_test");
            }
        }
    }
}
