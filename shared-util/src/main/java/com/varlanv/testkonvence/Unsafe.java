package com.varlanv.testkonvence;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;

public interface Unsafe<T, E extends Exception> {

    Optional<T> valueOptional();

    Unsafe<T, E> doOnSuccess(Consumer<T> successConsumer);

    <R> Unsafe<R, E> mapOnSuccess(Function<T, @NonNull R> successMapper);

    T unwrap(Function<E, ? extends RuntimeException> exceptionConsumer);

    T orElse(Supplier<T> defaultSupplier);

    Unsafe<T, E> onFail(Consumer<E> exceptionConsumer);

    static <T, E extends Exception> Unsafe<T, E> success(@NonNull T value) {
        Optional<@NonNull T> option = Optional.of(value);
        return new Unsafe<>() {
            @Override
            public Optional<T> valueOptional() {
                return option;
            }

            @Override
            public Unsafe<T, E> doOnSuccess(Consumer<T> successConsumer) {
                successConsumer.accept(value);
                return this;
            }

            @Override
            public <R> Unsafe<R, E> mapOnSuccess(Function<T, @NonNull R> successMapper) {
                R result = successMapper.apply(value);
                return Unsafe.<R, E>success(result);
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
            public Unsafe<T, E> onFail(Consumer<E> exceptionConsumer) {
                return this;
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
            public Unsafe<T, E> doOnSuccess(Consumer<T> successConsumer) {
                return this;
            }

            @Override
            public <R> Unsafe<R, E> mapOnSuccess(Function<T, R> successMapper) {
                return fail(exception);
            }

            @Override
            public T unwrap(Function<E, ? extends RuntimeException> exceptionConsumer) {
                throw exceptionConsumer.apply(exception);
            }

            @Override
            public T orElse(Supplier<T> defaultSupplier) {
                return defaultSupplier.get();
            }

            @Override
            public Unsafe<T, E> onFail(Consumer<E> exceptionConsumer) {
                exceptionConsumer.accept(exception);
                return this;
            }
        };
    }
}
