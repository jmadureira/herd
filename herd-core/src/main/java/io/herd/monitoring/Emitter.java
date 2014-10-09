package io.herd.monitoring;

public interface Emitter<E> {

    void emit(E event);
}
