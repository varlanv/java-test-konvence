package com.varlanv.testkonvence.proc;

import org.immutables.value.Value;

@Value.Immutable
interface Pair<T1, T2> {

    @Value.Parameter
    T1 left();

    @Value.Parameter
    T2 right();
}
