package io.herd.base;

import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * Collection of utility methods to help determine the size of attributes.
 */
public final class Sizes {

    /**
     * Returns the size of a <code>byte</code> primitive in bytes.
     * 
     * @param i any <code>byte</code> value.
     * @return {@link Byte#SIZE}.
     */
    public static final int sizeOf(byte i) {
        return Byte.BYTES;
    }

    /**
     * Returns the size of an {@link InetAddress} in bytes.
     * 
     * @param i an valid {@link InetAddress}.
     * @return 4 if its an {@link Inet4Address} or 16 otherwise.
     */
    public static final int sizeOf(InetAddress address) {
        if (address instanceof Inet4Address) {
            return 4;
        } else {
            return 16;
        }
    }

    /**
     * Returns the size of an <code>int</code> primitive in bytes.
     * 
     * @param i any <code>int</code> value.
     * @return {@link Integer#BYTES}
     */
    public static final int sizeOf(int i) {
        return Integer.BYTES;
    }

    /**
     * Returns the size of a <code>long</code> primitive in bytes.
     * 
     * @param i any <code>long</code> value.
     * @return {@link Long#BYTES}
     */
    public static final int sizeOf(long i) {
        return Long.BYTES;
    }

    /**
     * Returns the size of a <code>short</code> primitive in bytes.
     * 
     * @param i any <code>short</code> value.
     * @return {@link Short#BYTES}
     */
    public static final int sizeOf(short i) {
        return Short.BYTES;
    }
}
