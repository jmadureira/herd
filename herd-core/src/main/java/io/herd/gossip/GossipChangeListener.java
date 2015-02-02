package io.herd.gossip;

import java.net.InetSocketAddress;

public interface GossipChangeListener {

    /**
     * Notification sent when a changes was detected on a node identified by the given {@link InetSocketAddress}.
     * 
     * @param address The {@link InetSocketAddress} of the node whose state changed
     * @param state The changed {@link ApplicationState}
     * @param value The new {@link VersionedValue} of the state
     */
    void onNodeChange(InetSocketAddress address, ApplicationState state, VersionedValue value);

    /**
     * Notification sent when a new node identified by the given {@link InetSocketAddress} entered the cluster.
     * 
     * @param address The {@link InetSocketAddress} of the new node
     * @param state The {@link EndpointState} state of the node
     */
    void onNodeAdded(InetSocketAddress address, EndpointState state);

}
