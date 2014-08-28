package io.herd.base;

/**
 * Simple static methods modeled after Guava's Preconditions to test for argument sanity.
 */
public final class Preconditions {

    /**
     * Ensure that the provided elem is not null, otherwise a {@link NullPointerException} is thrown with the provided
     * message.
     * 
     * @param elem The element being checked.
     * @param message The error message to provide in case the element is null.
     * @return The checked elem.
     * @throws NullPointerException if the elem is null.
     */
    public static final <T> T checkNotNull(T elem, Object message) {
        if (elem == null) {
            /*
             * too lay to check if JIT is smart enough to know that we would be doing to null checks if using
             * String.valueOf()
             */
            throw new NullPointerException(message.toString());
        }
        return elem;
    }

    /**
     * Ensures that the provided expression is <code>true</code>, otherwise throws an {@link IllegalArgumentException}
     * with the provided message.
     * 
     * @param expression The expression being validated.
     * @param message The error message in case the expression resolves to <code>false</code>.
     * @throws IllegalArgumentException if the expression resolves to <code>false</code>.
     */
    public static final void checkState(boolean expression, Object message) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(message));
        }
    }

    private Preconditions() {

    }
}
