package io.herd.base;

/**
 * Helper class to deal with object and primitive equality. The normal usage of this class starts by fetching an
 * instance using either {@link #from()} or {@link #from(boolean)} with subsequent calls to anyone of the
 * {@link #append(long, long)} methods. The general contract of this class is that:
 * <ul>
 * <li>{@link #isEquals()} is always <code>true</code> as long as all previous calls to <code>append</code> are
 * <code>true</code></li>
 * <li>{@link #isEquals()} is always <code>false</code> if at least one call to <code>append</code> if
 * <code>false</code></li>
 * <li>if a call to <code>append</code> is <code>false</code> subsequent calls to <code>append</code> will be no-op
 * since the result will never change</li>
 * </ul>
 * Using the {@link #from()} and {@link #from(boolean)} prevents the system from creating unecessary objects since all
 * {@link Equality} instance are immutable.
 * 
 * @author joaomadureira
 *
 */
public final class Equality {

    public static final Equality from() {
        return from(true);
    }

    public static final Equality from(boolean isEquals) {
        return isEquals ? TRUE : FALSE;
    }

    private static final Equality FALSE = new Equality(false);

    private static final Equality TRUE = new Equality(true);

    private final boolean isEquals;

    private Equality(boolean isEquals) {
        this.isEquals = isEquals;
    }

    /**
     * Checks if 2 long values are equal.
     * 
     * @param l1 the left hand operand.
     * @param l2 the right hand operand.
     * @return A {@link Equality} to allow chaining.
     */
    public Equality append(long l1, long l2) {
        if (!isEquals) {
            return this;
        }
        return l1 == l2 ? TRUE : FALSE;
    }

    /**
     * Checks if 2 objects are equals using their {@link #equals(Object)} method.
     * 
     * @param obj1 the left hand object
     * @param obj2 the right hand object
     * @return A {@link Equality} to allow chaining.
     */
    public Equality append(Object obj1, Object obj2) {
        if (!isEquals || obj1 == obj2) {
            return this;
        }
        if (obj1 == null || obj2 == null) {
            return FALSE;
        }
        return obj1.equals(obj2) ? TRUE : FALSE;
    }

    public boolean isEquals() {
        return isEquals;
    }
}
