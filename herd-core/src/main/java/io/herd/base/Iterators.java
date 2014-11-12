package io.herd.base;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public final class Iterators {

    private static class CompositeIterator<U, T> implements Iterator<T> {

        private final Iterator<U> outerIterator;
        private final Function<U, Iterator<T>> innerF;
        private Iterator<T> currentIterator;

        public CompositeIterator(Iterator<U> outerIterator, Function<U, Iterator<T>> innerFunction) {
            Preconditions.checkNotNull(outerIterator, "Cannot create a composite iterator from a null reference");
            this.outerIterator = outerIterator;
            this.innerF = innerFunction;
        }

        @Override
        public boolean hasNext() {
            if (currentIterator == null || !currentIterator.hasNext()) {
                if (outerIterator.hasNext()) {
                    currentIterator = innerF.apply(outerIterator.next());
                } else {
                    return false;
                }
            }
            return currentIterator.hasNext();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return currentIterator.next();
        }

    }

    /**
     * Returns an iterator designed to iterate through elements of type <code>O</code> belonging to type <code>I</code>.
     * <p/>
     * This method is lazy so no changes are made to neither {@link #outerIterator} nor any of the inner elements.
     * 
     * @param outerIterator The iterator for the outer elements.
     * @param innerFunction The function used to return an {@link Iterator} for the inner elements.
     * @return An iterator for elements of type <code>O</code>.
     */
    public static final <I, O> Iterator<O> composite(Iterator<I> outerIterator, Function<I, Iterator<O>> innerFunction) {
        return new CompositeIterator<I, O>(outerIterator, innerFunction);
    }

    private Iterators() {

    }

}
