package com.varlanv.testkonvence;

import java.util.function.Consumer;

public interface UnsafeAction<E extends Exception> {

    void onSuccess(Runnable onSuccessAction);

    void onFail(Consumer<E> exceptionConsumer);

    static <E extends Exception> UnsafeAction<E> success() {
        return new UnsafeAction<>() {

            @Override
            public void onSuccess(Runnable onSuccessAction) {
                onSuccessAction.run();
            }

            @Override
            public void onFail(Consumer<E> exceptionConsumer) {
                // no-op
            }
        };
    }

    static <E extends Exception> UnsafeAction<E> fail(E exception) {
        return new UnsafeAction<E>() {

            @Override
            public void onSuccess(Runnable onSuccessAction) {
                // no-op
            }

            @Override
            public void onFail(Consumer<E> exceptionConsumer) {
                exceptionConsumer.accept(exception);
            }
        };
    }
}
