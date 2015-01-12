package io.herd.base;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;

/**
 * Utility class to work with da interwebs. Networks and stuff.
 *
 */
public final class Interwebs {

    private Interwebs() {

    }

    public static final InetAddress toInetAddress(String address) {
        try {
            return InetAddress.getByName(address);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create an InetAddress from " + address, e);
        }
    }

    /**
     * Creates an {@link InetSocketAddress} from the given address. The address has to include both the hostname or IP
     * address and the port number. Valid addresses include for instance:
     * <ul>
     * <li>127.0.0.1:8080</li>
     * <li>www.someaddress.com:443</li>
     * </ul>
     * 
     * @param address A valid non-null address
     * @return An {@link InetSocketAddress} resolved from the provided address.
     * @throws IllegalArgumentException if the address is invalid for some reason.
     */
    public static final InetSocketAddress toSocketAddress(String address) {
        Preconditions.checkNotEmpty(address, "Cannot create an InetSocketAddress from an empty string.");
        int index = address.indexOf(":");
        Preconditions.checkPositive(index, "Address must include a port number");
        try {
            String adr = address.substring(0, index);
            String port = address.substring(index + 1);
            return new InetSocketAddress(toInetAddress(adr), Integer.valueOf(port));
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to create an InetSocketAddress from " + address, e);
        }
    }

    public static final boolean isLocalHost(InetSocketAddress address) {
        return isLocalHost(address.getAddress());
    }

    public static final boolean isLocalHost(InetAddress address) {
        try {
            // quick check if the address is any valid local address or a loopback address
            if (address.isAnyLocalAddress() || address.isLoopbackAddress()) {
                return true;
            }
            // check if the address belong to any network interface
            return NetworkInterface.getByInetAddress(address) != null;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to determine localhost address", e);
        }
    }

    public static final boolean isNotLocalHost(InetAddress address) {
        return !isLocalHost(address);
    }

    public static final boolean isNotLocalHost(InetSocketAddress address) {
        return !isLocalHost(address);
    }
}
