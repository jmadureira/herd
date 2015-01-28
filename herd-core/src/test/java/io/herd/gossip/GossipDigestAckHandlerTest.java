package io.herd.gossip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GossipDigestAckHandlerTest implements GossipTestable {

    private static EventLoopGroup group;

    private EmbeddedChannel sc;
    private Gossiper gossiper;

    private InetSocketAddress localhost;

    private InetSocketAddress nodeAddress;

    @BeforeClass
    public static void createGroup() {
        group = new NioEventLoopGroup();
    }

    @AfterClass
    public static void destroyGroup() throws Exception {
        group.shutdownGracefully().sync();
    }

    @Before
    public void setUp() throws Exception {
        this.localhost = new InetSocketAddress(InetAddress.getLocalHost(), 8080);
        this.gossiper = new Gossiper();
        this.gossiper.start(localhost);
        this.sc = new EmbeddedChannel(new GossipDigestAckHandler(gossiper));
        this.nodeAddress = new InetSocketAddress(InetAddress.getByName("www.google.com"), 8080);
    }
    
    @Test
    public void testEmptyResponse() {
        List<GossipDigest> gDigests = new ArrayList<>();
        Map<InetSocketAddress, EndpointState> nodeStateMap = new HashMap<>();
        
        this.sc.writeInbound(new GossipDigestAck(gDigests, nodeStateMap));
        assertTrue(this.sc.finish());
        
        GossipDigestAck2 ack = (GossipDigestAck2) this.sc.readOutbound();
        assertTrue(ack.getEndpointStates().isEmpty());
        assertTrue(this.gossiper.liveNodes.isEmpty());
        assertEquals(1, this.gossiper.endpointStateMap.size()); // the local state
    }
    
    @Test
    public void testAddUnknownRemoteNode() {
        int generation = (int) (System.currentTimeMillis() / 1000);
        List<GossipDigest> gDigests = new ArrayList<>();
        Map<InetSocketAddress, EndpointState> nodeStateMap = new HashMap<>();
        nodeStateMap.put(nodeAddress, createEndpointState(generation));

        this.sc.writeInbound(new GossipDigestAck(gDigests, nodeStateMap));
        assertTrue(this.sc.finish());

        GossipDigestAck2 ack = (GossipDigestAck2) this.sc.readOutbound();
        assertTrue(ack.getEndpointStates().isEmpty());
        assertEquals(1, this.gossiper.liveNodes.size());
        assertEquals(2, this.gossiper.endpointStateMap.size());
        assertEquals(nodeStateMap.get(nodeAddress).toString(), this.gossiper.endpointStateMap.get(nodeAddress)
                .toString());
    }
    
    @Test
    public void testUpdateLocalNode() {
        int generation = (int) (System.currentTimeMillis() / 1000);
        List<GossipDigest> gDigests = new ArrayList<>();
        Map<InetSocketAddress, EndpointState> nodeStateMap = new HashMap<>();
        Map<ApplicationState, VersionedValue> appStateMap = new HashMap<>();
        appStateMap.put(ApplicationState.SERVICE_NAME, new VersionedValue("new Service"));
        nodeStateMap.put(nodeAddress, createEndpointState(generation, appStateMap));

        this.gossiper.liveNodes.add(nodeAddress);
        this.gossiper.endpointStateMap.put(nodeAddress, createEndpointState(generation));

        this.sc.writeInbound(new GossipDigestAck(gDigests, nodeStateMap));
        assertTrue(this.sc.finish());

        GossipDigestAck2 ack = (GossipDigestAck2) this.sc.readOutbound();
        assertTrue(ack.getEndpointStates().isEmpty());
        assertEquals(1, this.gossiper.liveNodes.size());
        assertEquals(2, this.gossiper.endpointStateMap.size());
        assertEquals(nodeStateMap.get(nodeAddress).getApplicationState(ApplicationState.SERVICE_NAME),
                this.gossiper.endpointStateMap.get(nodeAddress).getApplicationState(ApplicationState.SERVICE_NAME));
    }
    
    @Test
    public void testSendLocalNodeInfoBack() {
        int generation = (int) (System.currentTimeMillis() / 1000);
        Map<ApplicationState, VersionedValue> appStateMap = new HashMap<>();
        appStateMap.put(ApplicationState.SERVICE_NAME, new VersionedValue("new Service"));

        Map<InetSocketAddress, EndpointState> nodeStateMap = new HashMap<>();
        List<GossipDigest> gDigests = new ArrayList<>();
        gDigests.add(new GossipDigest(nodeAddress, generation, 0));
        this.gossiper.liveNodes.add(nodeAddress);
        this.gossiper.endpointStateMap.put(nodeAddress, createEndpointState(generation, appStateMap));

        this.sc.writeInbound(new GossipDigestAck(gDigests, nodeStateMap));
        assertTrue(this.sc.finish());

        GossipDigestAck2 ack = (GossipDigestAck2) this.sc.readOutbound();
        assertEquals(1, ack.getEndpointStates().size());
        assertEquals(1, this.gossiper.liveNodes.size());
        assertEquals(2, this.gossiper.endpointStateMap.size());
        assertEquals(
                this.gossiper.endpointStateMap.get(nodeAddress).getApplicationState(ApplicationState.SERVICE_NAME), ack
                        .getEndpointStates().get(nodeAddress).getApplicationState(ApplicationState.SERVICE_NAME));
    }

    @After
    public void tearDown() throws Exception {
        sc.close().sync();
    }

}
