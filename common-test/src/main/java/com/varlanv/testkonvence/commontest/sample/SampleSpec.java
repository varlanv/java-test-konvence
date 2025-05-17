package com.varlanv.testkonvence.commontest.sample;

import com.varlanv.testkonvence.commontest.BaseTest;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.Language;

@Getter(AccessLevel.PACKAGE)
@RequiredArgsConstructor
public class SampleSpec {

    private final List<SampleSources> sources;
    private final List<BaseTest.ThrowingConsumer<ConsumableSample>> extraAssertions;
    private final SampleOptions options;

    public SampleSpec() {
        this(List.of(), List.of(), SampleOptions.builder().build());
    }

    public SampleSpecFileStep withClass(String fullyQualifiedClassName) {
        var packageParts = fullyQualifiedClassName.split("\\.");
        if (packageParts.length == 0) {
            return new SampleSpecFileStep(this, fullyQualifiedClassName, "");
        }
        var className = packageParts[packageParts.length - 1];
        var packageName =
                fullyQualifiedClassName.substring(0, fullyQualifiedClassName.length() - className.length() - 1);
        return new SampleSpecFileStep(this, className, packageName);
    }

    @RequiredArgsConstructor
    public static class SampleSpecFileStep {

        private final SampleSpec spec;
        private final String outerClassName;
        private final String packageName;

        public SampleSpecSourceStep withJavaSources(@Language("Java") String sources) {
            return new SampleSpecSourceStep(spec, this, sources);
        }
    }

    @RequiredArgsConstructor
    public static class SampleSpecSourceStep {

        private final SampleSpec spec;
        private final SampleSpecFileStep parent;
        private final String sources;

        public SampleSpecFinish withExpectedTransformation(@Language("Java") String expectedTransformation) {
            return new SampleSpecFinish(spec, this, expectedTransformation);
        }
    }

    @RequiredArgsConstructor
    public static class SampleSpecFinish {

        private final SampleSpec spec;
        private final SampleSpecSourceStep parent;
        private final String expectedTransformation;
        List<BaseTest.ThrowingConsumer<ConsumableSample>> extraAssertions = new ArrayList<>(1);
        SampleOptions.SampleOptionsBuilder optionsBuilder = SampleOptions.builder();

        SampleSpec toSpec() {
            return new SampleSpec(
                    Stream.concat(
                                    spec.sources.stream(),
                                    Stream.of(ImmutableSampleSources.of(
                                            parent.parent.outerClassName,
                                            parent.parent.outerClassName + ".java",
                                            parent.parent.packageName,
                                            parent.sources,
                                            expectedTransformation)))
                            .toList(),
                    extraAssertions,
                    optionsBuilder.build());
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
