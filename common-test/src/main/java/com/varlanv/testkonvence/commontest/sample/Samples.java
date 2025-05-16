package com.varlanv.testkonvence.commontest.sample;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Samples {

    Stream<Sample> stream();

    Samples describe(String description, Function<SampleSpec, SampleSpec.SampleSpecFinish> specConsumer);

    Samples describeMany(Stream<Map.Entry<String, Function<SampleSpec, SampleSpec.SampleSpecFinish>>> specConsumers);

    Samples merge(Samples other);

    static Samples fromIterable(Iterable<Sample> origin) {
        var samples = new ArrayList<Sample>(
            origin instanceof Collection<Sample>
                ? ((Collection<Sample>) origin).size()
                : 10);
        origin.forEach(samples::add);
        return new SamplesFromIterable(samples);
    }

    static Samples samples() {
        return Samples.fromIterable(Collections.emptyList());
    }
}

class SamplesFromIterable implements Samples {

    private final List<Sample> samples;
    private final LinkedHashSet<String> uniqueSamplesDescriptions = new LinkedHashSet<>();

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
        samples.add(
            new Sample(
                description,
                resultSpec.sources(),
                resultSpec.extraAssertions(),
                resultSpec.options()
            )
        );
        return this;
    }

    @Override
    public Samples describeMany(Stream<Map.Entry<String, Function<SampleSpec, SampleSpec.SampleSpecFinish>>> specConsumers) {
        specConsumers.forEach(specEntry -> {
                var resultSpec = specEntry.getValue().apply(new SampleSpec()).toSpec();
                samples.add(
                    new Sample(
                        specEntry.getKey(),
                        resultSpec.sources(),
                        resultSpec.extraAssertions(),
                        resultSpec.options()
                    )
                );
            }
        );
        return this;
    }

    @Override
    public Samples merge(Samples other) {
        var newSamples = new ArrayList<>(samples);
        other.stream().forEach(newSamples::add);
        return new SamplesFromIterable(newSamples);
    }
}
