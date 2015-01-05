package io.herd.base;

/**
 * Simple static methods modeled after Guava's Preconditions to test for argument sanity.
 */
public final class Preconditions {

    /**
     * Ensures that the provided array has the exact specified size. Note that this method checks the length of the
     * array and not it's contents.
     * 
     * @param elements An array being validated
     * @param size The expected size of the array
     * @param message The error message to show in case the <code>elements</code> array isn't valid.
     * @return elements the provided array
     * @throws NullPointerException if the array is null
     * @throws IllegalArgumentException of the array doesn't have the expected size
     */
    public static final Object[] checkArraySize(Object[] elements, int size, Object message) {
        if (checkNotNull(elements, message).length != size) {
            throw new IllegalArgumentException(String.valueOf(message));
        }
        return elements;
    }

    /**
     * Ensures that the provided string <code>elem</code> isn't empty i.e. is not <code>null</code> and has content.
     * 
     * @param elem A string being validated
     * @param message The error message to show in case the <code>elem</code> string isn't valid.
     * @param elem The valid provided string.
     * @throws IllegalArgumentException if the <code>elem</code> string isn't valid.
     */
    public static final String checkNotEmpty(String elem, Object message) {
        if (elem == null || elem.isEmpty()) {
            throw new IllegalArgumentException(String.valueOf(message));
        }
        return elem;
    }

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
             * too lazy to check if JIT is smart enough to know that we would be doing tull checks if using
             * String.valueOf()
             */
            throw new NullPointerException(message.toString());
        }
        return elem;
    }

    /**
     * Ensures that the provided <code>number</code> is a positive integer and not zero.
     * 
     * @param number The <code>number</code> being validated.
     * @param message The error message to show in case the <code>number</code> is negative or zero.
     * @throws IllegalArgumentException of the validation fails.
     */
    public static final void checkPositive(int number, Object message) {
        if (number <= 0) {
            throw new IllegalArgumentException(String.valueOf(message));
        }
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

    /**
     * Ensures that the provided value is within a specified range.
     * 
     * @param value The value being checked
     * @param min The minimum allowed value (inclusive)
     * @param max The maximum allowed value (exclusive)
     * @param message The error message to show in case the provided value isn't valid.
     * @return value the provided value
     * @throws IllegalArgumentException if the value isn't valid
     */
    public static final int checkValueRange(int value, int min, int max, Object message) {
        if (value <= min || value >= max) {
            throw new IllegalArgumentException(String.valueOf(message));
        }
        return value;
    }

    private Preconditions() {

    }
}
