package com.varlanv.testkonvence;

import org.jspecify.annotations.NonNull;

public final class TrustedException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String message;

    public TrustedException(String message) {
        super(message);
        this.message = message;
    }

    public TrustedException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    @Override
    @NonNull public String getMessage() {
        return message;
    }
}
