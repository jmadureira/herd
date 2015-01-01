package io.herd.base;

import java.util.Collection;

public final class Collections {

    private Collections() {

    }

    /**
     * Checks if the given collection is empty. A <code>null</code> collection is considered to be empty.
     * 
     * @param collection A possibility <code>null</code> collection.
     * @return <code>true</code> iff the collection is either <code>null</code> or empty.
     * @see Collection#isEmpty()
     */
    public static final boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
