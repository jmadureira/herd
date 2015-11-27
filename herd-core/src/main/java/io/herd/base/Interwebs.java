package io.herd.base;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;

/**
 * Utility class to work with da interwebs. Networks and stuff.
 *
 */
public final class Interwebs {

    /**
     * Returns a random port number that isn't being used at the moment by any process.
     * 
     * @return A free port
     * @throws IllegalStateException if no free port was found
     * @see ServerSocket
     */
    public static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to find a free port", e);
        }
    }
    
    public static boolean isLocalHost(InetAddress address) {
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

    public static boolean isLocalHost(InetSocketAddress address) {
        return isLocalHost(address.getAddress());
    }

    public static boolean isNotLocalHost(InetAddress address) {
        return !isLocalHost(address);
    }

    public static boolean isNotLocalHost(InetSocketAddress address) {
        return !isLocalHost(address);
    }

    public static InetAddress toInetAddress(String address) {
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
    public static InetSocketAddress toSocketAddress(String address) {
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

    private Interwebs() {

    }
}
