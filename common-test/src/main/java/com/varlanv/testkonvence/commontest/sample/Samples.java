package com.varlanv.testkonvence.commontest.sample;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Samples {

    Stream<Sample> stream();

    Samples describe(String description, Function<SampleSpec, SampleSpec.SampleSpecFinish> specConsumer);

    Samples describeMany(Stream<Map.Entry<String, Function<SampleSpec, SampleSpec.SampleSpecFinish>>> specConsumers);

    static Samples samples() {
        var samples = new ArrayList<Sample>();
        var uniqueSamplesDescriptions = new LinkedHashSet<>();
        return new Samples() {

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
        };
    }
}
