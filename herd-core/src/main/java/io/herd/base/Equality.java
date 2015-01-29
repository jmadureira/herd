package io.herd.base;

public class EqualsBuilder {

    private boolean isEquals = true;

    public EqualsBuilder() {

    }

    public boolean isEquals() {
        return isEquals;
    }

    /**
     * Checks if 2 long values are equal.
     * 
     * @param l1 the left hand operand.
     * @param l2 the right hand operand.
     * @return This {@link EqualsBuilder} to allow chaining.
     */
    public EqualsBuilder append(long l1, long l2) {
        if (!isEquals) {
            return this;
        }
        isEquals = l1 == l2;
        return this;
    }

    /**
     * Checks if 2 objects are equals using their {@link #equals(Object)} method.
     * 
     * @param obj1 the left hand object
     * @param obj2 the right hand object
     * @return This {@link EqualsBuilder} to allow chaining.
     */
    public EqualsBuilder append(Object obj1, Object obj2) {
        if (!isEquals || obj1 == obj2) {
            return this;
        }
        if (obj1 == null || obj2 == null) {
            isEquals = false;
            return this;
        }
        isEquals = obj1.equals(obj2);
        return this;
    }
}
