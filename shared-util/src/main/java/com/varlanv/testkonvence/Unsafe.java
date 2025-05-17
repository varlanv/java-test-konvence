package com.varlanv.testkonvence;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;

public interface Unsafe<T, E extends Exception> {

    Optional<T> valueOptional();

    void onSuccess(Consumer<T> successConsumer);

    T unwrap(Function<E, ? extends RuntimeException> exceptionConsumer);

    T orElse(Supplier<T> defaultSupplier);

    void onFail(Consumer<E> exceptionConsumer);

    static <T, E extends Exception> Unsafe<T, E> success(@NonNull T value) {
        Optional<@NonNull T> option = Optional.of(value);
        return new Unsafe<>() {
            @Override
            public Optional<T> valueOptional() {
                return option;
            }

            @Override
            public void onSuccess(Consumer<T> successConsumer) {
                successConsumer.accept(value);
            }

            @Override
            public T unwrap(Function<E, ? extends RuntimeException> exceptionConsumer) {
                return value;
            }

            @Override
            public T orElse(Supplier<T> defaultSupplier) {
                return value;
            }

            @Override
            public void onFail(Consumer<E> exceptionConsumer) {
                // no-op
            }
        };
    }

    static <T, E extends Exception> Unsafe<T, E> fail(E exception) {
        return new Unsafe<>() {
            @Override
            public Optional<T> valueOptional() {
                return Optional.<T>empty();
            }

            @Override
            public void onSuccess(Consumer<T> successConsumer) {}

            @Override
            public T unwrap(Function<E, ? extends RuntimeException> exceptionConsumer) {
                throw exceptionConsumer.apply(exception);
            }

            @Override
            public T orElse(Supplier<T> defaultSupplier) {
                return defaultSupplier.get();
            }

            @Override
            public void onFail(Consumer<E> exceptionConsumer) {
                exceptionConsumer.accept(exception);
            }
        };
    }
}
