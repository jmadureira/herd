package io.herd.gossip;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import io.herd.ServerRuntime;

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
    
    private static final int DEFAULT_PORT = 8080;
    
    private ServerRuntime runtime;
    private DefaultGossipConfiguration configuration;

    private InetSocketAddress nodeAddress;
    private InetSocketAddress localhost;

    
    @Before
    public void setUp() throws Exception {
        this.localhost = new InetSocketAddress(InetAddress.getLocalHost(), 8080);
        Set<String> seedNodes = new HashSet<>();
        seedNodes.add("127.0.0.1:8080");
        this.configuration = new DefaultGossipConfiguration();
        configuration.setPort(DEFAULT_PORT);
        configuration.setSeedNodes(seedNodes);
        this.nodeAddress = new InetSocketAddress("173.194.66.147", 8080);
    }
    
    @After
    public void tearDown() {
        if(runtime != null) {
            runtime.stop();
        }
    }
    
    @Test
    public void testGossipRoundService() {
        
        int generation = (int) (System.currentTimeMillis() / 1000);
        long maxVersion = System.currentTimeMillis();
        Map<ApplicationState, VersionedValue> states = new HashMap<>();
        addApplicationState(states, ApplicationState.SERVICE_NAME, "someService", maxVersion);
        Gossiper gossiper = new Gossiper(localhost);
        
        gossiper.liveNodes.add(nodeAddress);
        gossiper.endpointStateMap.put(nodeAddress, createEndpointState(generation, states));
        
        this.runtime = new Gossip(configuration).build();
        
        runtime.start();
        
        GossipClient client = new GossipClient(gossiper);
        
        // first send our current state with the new node
        List<GossipDigest> digests = new ArrayList<>();
        digests.add(new GossipDigest(nodeAddress, generation, maxVersion));
        GossipDigestSyn message = new GossipDigestSyn(digests);
        
        client.gossip(message, new InetSocketAddress("127.0.0.1", DEFAULT_PORT));
        
        // then downgrade our state
        // the gossip round should restore our state
        gossiper.endpointStateMap.remove(nodeAddress);
        assertFalse(gossiper.endpointStateMap.containsKey(nodeAddress));
        
        digests = new ArrayList<>();
        digests.add(new GossipDigest(nodeAddress, generation, 0));
        message = new GossipDigestSyn(digests);
        
        client.gossip(message, new InetSocketAddress("127.0.0.1", DEFAULT_PORT));
        
        assertTrue(gossiper.endpointStateMap.containsKey(nodeAddress));
    }
}
