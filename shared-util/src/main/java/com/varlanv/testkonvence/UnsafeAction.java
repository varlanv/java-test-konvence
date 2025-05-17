package com.varlanv.testkonvence;

import java.util.function.Consumer;

public interface UnsafeAction<E extends Exception> {

    UnsafeAction<E> onSuccess(Runnable onSuccessAction);

    UnsafeAction<E> onFail(Consumer<E> exceptionConsumer);

    static <E extends Exception> UnsafeAction<E> success() {
        return new UnsafeAction<>() {

            @Override
            public UnsafeAction<E> onSuccess(Runnable onSuccessAction) {
                onSuccessAction.run();
                return this;
            }

            @Override
            public UnsafeAction<E> onFail(Consumer<E> exceptionConsumer) {
                return this;
            }
        };
    }

    static <E extends Exception> UnsafeAction<E> fail(E exception) {
        return new UnsafeAction<E>() {

            @Override
            public UnsafeAction<E> onSuccess(Runnable onSuccessAction) {
                return this;
            }

            @Override
            public UnsafeAction<E> onFail(Consumer<E> exceptionConsumer) {
                exceptionConsumer.accept(exception);
                return this;
            }
        };
    }
}
