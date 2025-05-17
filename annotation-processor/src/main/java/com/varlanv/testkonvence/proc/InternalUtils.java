package com.varlanv.testkonvence.proc;

interface InternalUtils {

    @SuppressWarnings("unchecked")
    static <T extends Throwable, R> R hide(Throwable t) throws T {
        throw (T) t;
    }
}
