package io.herd.gossip;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import io.herd.ServerRuntime;
import io.herd.base.Interwebs;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GossipTest implements GossipTestable {

    private ServerRuntime runtime;
    private DefaultGossipConfiguration configuration;

    private InetSocketAddress nodeAddress;
    private InetSocketAddress localhost;

    @Before
    public void setUp() throws Exception {
        int port = Interwebs.findFreePort();
        this.localhost = new InetSocketAddress(InetAddress.getLocalHost(), port);
        Set<String> seedNodes = new HashSet<>();
        seedNodes.add("127.0.0.1:" + port);
        this.configuration = new DefaultGossipConfiguration();
        configuration.setPort(port);
        configuration.setSeedNodes(seedNodes);
        this.runtime = new Gossip(configuration).build();
        runtime.start();
        this.nodeAddress = new InetSocketAddress("127.0.0.1", Interwebs.findFreePort());
    }

    @After
    public void tearDown() {
        if (runtime != null) {
            runtime.stop();
        }
    }
    
    @Test
    public void testNewNodeGossipingForTheFirstTime() throws Exception {

        int generation = (int) (System.currentTimeMillis() / 1000);
        long maxVersion = System.currentTimeMillis();
        Map<ApplicationState, VersionedValue> states = new HashMap<>();
//        addApplicationState(states, ApplicationState.SERVICE_NAME, "someService", maxVersion);
        Gossiper gossiper = new Gossiper(localhost);

        gossiper.liveNodes.add(nodeAddress);
        gossiper.endpointStateMap.put(nodeAddress, createEndpointState(generation, states));

        GossipClient client = new GossipClient(gossiper);

        // first send our current state with the new node
        List<GossipDigest> digests = new ArrayList<>();
        digests.add(new GossipDigest(nodeAddress, generation, maxVersion));
        GossipDigestSyn message = new GossipDigestSyn(digests);

        client.gossip(message, new InetSocketAddress(InetAddress.getLocalHost(), this.localhost.getPort()));

        // then downgrade our state
        // the gossip round should restore our state
        gossiper.endpointStateMap.remove(nodeAddress);
        assertFalse(gossiper.endpointStateMap.containsKey(nodeAddress));

        digests = new ArrayList<>();
        digests.add(new GossipDigest(nodeAddress, generation, 0));
        message = new GossipDigestSyn(digests);

        client.gossip(message, new InetSocketAddress(InetAddress.getLocalHost(), this.localhost.getPort()));

        assertTrue(gossiper.endpointStateMap.containsKey(nodeAddress));
    }

    @Test
    public void testRandomGossipRoundService() throws Exception {

        int generation = (int) (System.currentTimeMillis() / 1000);
        long maxVersion = System.currentTimeMillis();
        Map<ApplicationState, VersionedValue> states = new HashMap<>();
        addApplicationState(states, ApplicationState.SERVICE_NAME, "someService", maxVersion);
        Gossiper gossiper = new Gossiper(localhost);

        gossiper.liveNodes.add(nodeAddress);
        gossiper.endpointStateMap.put(nodeAddress, createEndpointState(generation, states));

        GossipClient client = new GossipClient(gossiper);

        // first send our current state with the new node
        List<GossipDigest> digests = new ArrayList<>();
        digests.add(new GossipDigest(nodeAddress, generation, maxVersion));
        GossipDigestSyn message = new GossipDigestSyn(digests);

        client.gossip(message, new InetSocketAddress(InetAddress.getLocalHost(), this.localhost.getPort()));

        // then downgrade our state
        // the gossip round should restore our state
        gossiper.endpointStateMap.remove(nodeAddress);
        assertFalse(gossiper.endpointStateMap.containsKey(nodeAddress));

        digests = new ArrayList<>();
        digests.add(new GossipDigest(nodeAddress, generation, 0));
        message = new GossipDigestSyn(digests);

        client.gossip(message, new InetSocketAddress(InetAddress.getLocalHost(), this.localhost.getPort()));

        assertTrue(gossiper.endpointStateMap.containsKey(nodeAddress));
    }
}
