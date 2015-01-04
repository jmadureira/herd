package io.herd.base;

/**
 * General contract for classes that are responsible for building instances.
 * 
 * @param <T> The type of object being built.
 */
public interface Builder<T> {

    /**
     * 
     * @return A new instance of type <code>T</code>.
     * @throws IllegalStateException if the builder is unable to build an instance of type <code>T</code>.
     */
    T build();
}
