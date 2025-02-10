package com.varlanv.testkonvence;

import com.varlanv.testkonvence.commontest.UnitTest;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ClassNameFromDisplayNameTest implements UnitTest {

    @TestFactory
    Stream<DynamicTest> defaultDataTable() {
        return Stream.of(
                Map.entry("", ""),
                Map.entry("   ", ""),
                Map.entry("1", ""),
                Map.entry("12345", ""),
                Map.entry("_123_", ""),
                Map.entry("___", ""),
                Map.entry("Some good fixture name", "SomeGoodFixtureName"),
                Map.entry("Some good fixture name, which has comas, dots., question? marks, and exclamation marks!", "SomeGoodFixtureNameWhichHasComasDotsQuestionMarksAndExclamationMarks"),
                Map.entry("Some123good456fixture789name0", "Some123good456fixture789name0"),
                Map.entry("Слава Україні", ""),
                Map.entry("qwe", "Qwe"),
                Map.entry("___abc___", "Abc"),
                Map.entry("a_b_c_", "ABC"),
                Map.entry("a_b_c", "ABC"),
                Map.entry("-a_b_c-", "ABC"),
                Map.entry("a_b_c-", "ABC"),
                Map.entry("-a_b_c", "ABC"),
                Map.entry("Some good\n multiline   fixture\n name, which also has multiple whitespaces and_underscores   ", "SomeGoodMultilineFixtureNameWhichAlsoHasMultipleWhitespacesAndUnderscores")
            )
            .map(entry -> DynamicTest.dynamicTest(
                    "Display name \"%s\" should be converted to class name \"%s\"".formatted(entry.getKey(), entry.getValue()),
                    () -> {
                        var expectedClassName = entry.getValue();
                        var displayName = entry.getKey();
                        var actualClassName = new ClassNameFromDisplayName(
                            displayName,
                            "AnyOriginalClassName"
                        ).newName();
                        assertThat(actualClassName).isEqualTo(expectedClassName);
                    }
                )
            );
    }
}
