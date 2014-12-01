package io.herd.base;

public final class Maths {

    /**
     * Returns the logarithm of a double value for a given base. Special cases:
     * <ul>
     * <li>If the argument is NaN or less than zero, then the result is NaN.</li>
     * <li>If the argument is positive infinity, then the result is positive infinity.</li>
     * <li>If the argument is positive zero or negative zero, then the result is negative infinity.</li>
     * <ul>
     * The computed result must be within 1 ulp of the exact result. Results must be semi-monotonic.
     * 
     * @param base the logarithm base
     * @param value a value
     * @return the value log(value) for a given base.
     */
    public static double log(int base, double value) {
        return Math.log(value) / Math.log(base);
    }

    private Maths() {

    }
}
