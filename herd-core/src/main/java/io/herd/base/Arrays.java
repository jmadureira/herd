package io.herd.base;

/**
 * Extension to the {@link java.util.Arrays} class with additional set of helper methods to deal with arrays.
 * 
 * @author joaomadureira
 *
 */
public final class Arrays {

    /**
     * Swaps the elements at the specified positions in the specified array. Note that this methods won't do any
     * boundary checks.
     * <p/>
     * If the specified positions are equal, invoking this method leaves the array unchanged.
     * 
     * @param array The array being altered.
     * @param i the index of one element to be swapped.
     * @param j the index of the other element to be swapped.
     * @return The provided array with the elements swapped.
     * @throws ArrayIndexOutOfBoundsException if either <code>i</code> or <code>j</code> are outside the boundaries of
     *             the given array.
     */
    public static Object[] swap(Object[] array, int i, int j) {
        Object temp = array[i];
        array[i] = array[j];
        array[j] = temp;
        return array;
    }
}
