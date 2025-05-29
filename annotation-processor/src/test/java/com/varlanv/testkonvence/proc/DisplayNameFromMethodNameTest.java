package com.varlanv.testkonvence.proc;

import static org.assertj.core.api.Assertions.assertThat;

import com.varlanv.testkonvence.commontest.UnitTest;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class DisplayNameFromMethodNameTest implements UnitTest {

    @TestFactory
    @DisplayName("default data table")
    Stream<DynamicTest> defaultDataTable() {
        return Stream.of(
                        Map.entry("a_b_c", "a b c"),
                        Map.entry("abCd", "ab cd"),
                        Map.entry("abCdQwErTy", "ab cd qw er ty"),
                        Map.entry("ABC", "a b c"),
                        Map.entry("ab_cd_qwErTy", "ab cd 'qwErTy'"),
                        Map.entry("ab_qwErTy_cd_", "ab 'qwErTy' cd"),
                        Map.entry("when_something_then_should_something", "when something then should something"),
                        Map.entry(
                                "when_call_someMethod_then_should_something",
                                "when call 'someMethod' then should something"),
                        Map.entry("someMethod_should_do_something", "'someMethod' should do something"),
                        Map.entry("__someMethod_should_do_something__", "'someMethod' should do something"),
                        Map.entry("someMethod___should___do___something__", "'someMethod' should do something"),
                        Map.entry("___someMethod___should___do___something__", "'someMethod' should do something"),
                        Map.entry("___someMethod___shouldDo___someThing__", "'someMethod' 'shouldDo' 'someThing'"))
                .map(entry -> DynamicTest.dynamicTest(
                        "Method name \"%s\" should be converted to display name \"%s\""
                                .formatted(entry.getKey(), entry.getValue()),
                        () -> {
                            var expectedDisplayName = entry.getValue();
                            var methodName = entry.getKey();
                            var actualDisplayName = DisplayNameFromMethodName.convert(methodName);
                            assertThat(actualDisplayName).isEqualTo(expectedDisplayName);
                        }));
    }
}
