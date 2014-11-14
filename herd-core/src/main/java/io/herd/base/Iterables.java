package io.herd.base;

import java.util.Iterator;
import java.util.function.Function;

public final class Iterables {

    private Iterables() {

    }

    /**
     * Returns an {@link Iterable} designed to iterate through elements of type <code>O</code> belonging to type
     * <code>I</code>.
     * <p/>
     * This method is lazy so no changes are made to neither {@link #outerIterable} nor any of the inner elements.
     * 
     * @param outerIterable The iterator for the outer elements.
     * @param innerFunction The function used to return an {@link Iterator} for the inner elements.
     * @return An {@link Iterable} for elements of type <code>O</code>.
     */
    public static final <I, O> Iterable<O> composite(final Iterable<I> outerIterable,
            Function<I, Iterator<O>> innerFunction) {

        Preconditions.checkNotNull(outerIterable, "Cannot create a composite iterable from a null reference");
        return new Iterable<O>() {

            @Override
            public Iterator<O> iterator() {
                return Iterators.composite(outerIterable.iterator(), innerFunction);
            }
        };
    }
}
