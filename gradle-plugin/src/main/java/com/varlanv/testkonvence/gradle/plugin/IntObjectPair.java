package com.varlanv.testkonvence.gradle.plugin;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class IntObjectPair<T> {

    int left;
    T right;

    static <T> IntObjectPair<T> of(int left, T right) {
        return new IntObjectPair<T>(left, right);
    }
}
