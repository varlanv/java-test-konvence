package com.varlanv.testkonvence.commontest.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
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
        return fromIterable(Collections.emptyList());
    }
}
