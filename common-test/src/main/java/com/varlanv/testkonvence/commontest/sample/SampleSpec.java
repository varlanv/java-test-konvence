package com.varlanv.testkonvence.commontest.sample;

import com.varlanv.testkonvence.commontest.BaseTest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Getter(AccessLevel.PACKAGE)
@RequiredArgsConstructor
public class SampleSpec {

    List<SampleSources> sources;
    List<BaseTest.ThrowingConsumer<ConsumableSample>> extraAssertions;
    SampleOptions options;

    public SampleSpec() {
        this(List.of(), List.of(), SampleOptions.builder().build());
    }

    public SampleSpecFileStep withClass(String fullyQualifiedClassName) {
        var packageParts = fullyQualifiedClassName.split("\\.");
        if (packageParts.length == 0) {
            return new SampleSpecFileStep(this, fullyQualifiedClassName, "");
        }
        var className = packageParts[packageParts.length - 1];
        var packageName = fullyQualifiedClassName.substring(0, fullyQualifiedClassName.length() - className.length() - 1);
        return new SampleSpecFileStep(this, className, packageName);
    }

    @RequiredArgsConstructor
    public static class SampleSpecFileStep {

        SampleSpec spec;
        String outerClassName;
        String packageName;

        public SampleSpecSourceStep withJavaSources(@Language("Java") String sources) {
            return new SampleSpecSourceStep(spec, this, sources);
        }
    }

    @RequiredArgsConstructor
    public static class SampleSpecSourceStep {

        SampleSpec spec;
        SampleSpecFileStep parent;
        String sources;

        public SampleSpecFinish withExpectedTransformation(@Language("Java") String expectedTransformation) {
            return new SampleSpecFinish(spec, this, expectedTransformation);
        }
    }

    @RequiredArgsConstructor
    public static class SampleSpecFinish {

        SampleSpec spec;
        SampleSpecSourceStep parent;
        String expectedTransformation;
        List<BaseTest.ThrowingConsumer<ConsumableSample>> extraAssertions = new ArrayList<>(1);
        SampleOptions.SampleOptionsBuilder optionsBuilder = SampleOptions.builder();

        SampleSpec toSpec() {
            return new SampleSpec(
                Stream.concat(
                    spec.sources.stream(),
                    Stream.of(
                        new SampleSources(
                            parent.parent.outerClassName,
                            parent.parent.outerClassName + ".java",
                            parent.parent.packageName,
                            parent.sources,
                            expectedTransformation
                        )
                    )
                ).toList(),
                extraAssertions,
                optionsBuilder.build()
            );
        }

        public SampleSpecFinish withExtraAssertions(BaseTest.ThrowingConsumer<ConsumableSample> extraAssertion) {
            extraAssertions.add(extraAssertion);
            return this;
        }

        public SampleSpecFinish withOptions(Consumer<SampleOptions.SampleOptionsBuilder> action) {
            action.accept(optionsBuilder);
            return this;
        }

        public SampleSpec and() {
            return toSpec();
        }
    }
}