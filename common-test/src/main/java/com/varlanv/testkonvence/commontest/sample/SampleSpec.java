package com.varlanv.testkonvence.commontest.sample;

import com.varlanv.testkonvence.commontest.BaseTest;
import com.varlanv.testkonvence.commontest.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.intellij.lang.annotations.Language;

public class SampleSpec {

    private final ImmutableList<SampleSources> sources;
    private final ImmutableList<BaseTest.ThrowingConsumer<ConsumableSample>> extraAssertions;
    private final SampleOptions options;

    public ImmutableList<SampleSources> sources() {
        return sources;
    }

    public ImmutableList<BaseTest.ThrowingConsumer<ConsumableSample>> extraAssertions() {
        return extraAssertions;
    }

    public SampleOptions options() {
        return options;
    }

    public SampleSpec() {
        this(List.of(), List.of(), ImmutableSampleOptions.builder().build());
    }

    public SampleSpec(
            List<SampleSources> sources,
            List<BaseTest.ThrowingConsumer<ConsumableSample>> extraAssertions,
            SampleOptions options) {
        this.sources = ImmutableList.copyOf(sources);
        this.extraAssertions = ImmutableList.copyOf(extraAssertions);
        this.options = options;
    }

    public SampleSpecFileStep withClass(String fullyQualifiedClassName) {
        var packageParts = fullyQualifiedClassName.split("\\.", -1);
        if (packageParts.length == 0) {
            return new SampleSpecFileStep(this, fullyQualifiedClassName, "");
        }
        var className = packageParts[packageParts.length - 1];
        var packageName =
                fullyQualifiedClassName.substring(0, fullyQualifiedClassName.length() - className.length() - 1);
        return new SampleSpecFileStep(this, className, packageName);
    }

    public static class SampleSpecFileStep {

        private final SampleSpec spec;
        private final String outerClassName;
        private final String packageName;

        public SampleSpecFileStep(SampleSpec spec, String outerClassName, String packageName) {
            this.spec = spec;
            this.outerClassName = outerClassName;
            this.packageName = packageName;
        }

        public SampleSpecSourceStep withJavaSources(@Language("Java") String sources) {
            return new SampleSpecSourceStep(spec, this, sources);
        }
    }

    public static class SampleSpecSourceStep {

        private final SampleSpec spec;
        private final SampleSpecFileStep parent;
        private final String sources;

        public SampleSpecSourceStep(SampleSpec spec, SampleSpecFileStep parent, String sources) {
            this.spec = spec;
            this.parent = parent;
            this.sources = sources;
        }

        public SampleSpecFinish withExpectedTransformation(@Language("Java") String expectedTransformation) {
            return new SampleSpecFinish(spec, this, expectedTransformation);
        }
    }

    public static class SampleSpecFinish {

        private final SampleSpec spec;
        private final SampleSpecSourceStep parent;
        private final String expectedTransformation;
        List<BaseTest.ThrowingConsumer<ConsumableSample>> extraAssertions = new ArrayList<>(1);
        ImmutableSampleOptions.Builder optionsBuilder = ImmutableSampleOptions.builder();

        public SampleSpecFinish(SampleSpec spec, SampleSpecSourceStep parent, String expectedTransformation) {
            this.spec = spec;
            this.parent = parent;
            this.expectedTransformation = expectedTransformation;
        }

        SampleSpec toSpec() {
            return new SampleSpec(
                    Stream.concat(
                                    spec.sources.value().stream(),
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

        public SampleSpecFinish withOptions(Consumer<ImmutableSampleOptions.Builder> action) {
            action.accept(optionsBuilder);
            return this;
        }

        public SampleSpec and() {
            return toSpec();
        }
    }
}
