package com.varlanv.testkonvence.commontest.sample;

import com.varlanv.testkonvence.commontest.ImmutableList;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

class SamplesFromIterable implements Samples {

    private final List<Sample> samples;
    private final Set<String> uniqueSamplesDescriptions = new LinkedHashSet<>();

    SamplesFromIterable(List<Sample> origin) {
        this.samples = origin;
    }

    @Override
    public Stream<Sample> stream() {
        return samples.stream();
    }

    @Override
    public Samples describe(String description, Function<SampleSpec, SampleSpec.SampleSpecFinish> specConsumer) {
        if (!uniqueSamplesDescriptions.add(description)) {
            throw new IllegalArgumentException("Sample [%s] was already added previously".formatted(description));
        }
        var resultSpec = specConsumer.apply(new SampleSpec()).toSpec();
        samples.add(ImmutableSample.of(
                description,
                ImmutableList.copyOf(resultSpec.sources()),
                ImmutableList.copyOf(resultSpec.extraAssertions()),
                resultSpec.options()));
        return this;
    }

    @Override
    public Samples describeMany(
            Stream<Map.Entry<String, Function<SampleSpec, SampleSpec.SampleSpecFinish>>> specConsumers) {
        specConsumers.forEach(specEntry -> {
            var resultSpec = specEntry.getValue().apply(new SampleSpec()).toSpec();
            samples.add(ImmutableSample.of(
                    specEntry.getKey(),
                    ImmutableList.copyOf(resultSpec.sources()),
                    ImmutableList.copyOf(resultSpec.extraAssertions()),
                    resultSpec.options()));
        });
        return this;
    }

    @Override
    public Samples merge(Samples other) {
        var newSamples = new ArrayList<>(samples);
        other.stream().forEach(newSamples::add);
        return new SamplesFromIterable(newSamples);
    }
}
