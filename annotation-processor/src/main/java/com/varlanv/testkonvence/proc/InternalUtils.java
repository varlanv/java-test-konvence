package com.varlanv.testkonvence.proc;

import org.checkerframework.checker.nullness.qual.NonNull;

interface InternalUtils {

    @SuppressWarnings("unchecked")
    @NonNull static <T extends Throwable, R> R hide(Throwable t) throws T {
        throw (T) t;
    }

    @SuppressWarnings("unchecked")
    @NonNull static <T extends Throwable> T hideThrow(Throwable t) throws T {
        throw (T) t;
    }
}
