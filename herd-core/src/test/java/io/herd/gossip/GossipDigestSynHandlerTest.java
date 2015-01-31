package io.herd.gossip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GossipDigestSynHandlerTest implements GossipTestable {

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
        this.gossiper = new Gossiper(localhost);
        this.gossiper.start();
        this.sc = new EmbeddedChannel(new GossipDigestSynHandler(gossiper));
        this.nodeAddress = new InetSocketAddress(InetAddress.getByName("www.google.com"), 8080);
    }

    @Test
    public void testAskForUnknwonNode() throws Exception {
        int generation = (int) (System.currentTimeMillis() / 1000);
        List<GossipDigest> gDigests = new ArrayList<>();
        gDigests.add(new GossipDigest(nodeAddress, generation, 0));

        this.sc.writeInbound(new GossipDigestSyn(gDigests));
        assertTrue(this.sc.finish());

        GossipDigestAck ack = (GossipDigestAck) this.sc.readOutbound();
        assertEquals(gDigests, ack.digests);
        assertTrue(ack.getEndpointStates().isEmpty());
    }

    @Test
    public void testAskForRestartedNode() throws Exception {
        int generation = (int) (System.currentTimeMillis() / 1000);
        List<GossipDigest> gDigests = new ArrayList<>();
        gDigests.add(new GossipDigest(nodeAddress, generation + 1000, 0));

        this.gossiper.liveNodes.add(nodeAddress);
        this.gossiper.endpointStateMap.put(nodeAddress, createEndpointState(generation));

        this.sc.writeInbound(new GossipDigestSyn(gDigests));
        assertTrue(this.sc.finish());

        GossipDigestAck ack = (GossipDigestAck) this.sc.readOutbound();
        assertEquals(gDigests, ack.digests);
        assertTrue(ack.getEndpointStates().isEmpty());
    }

    @Test
    public void testDoNotAskForAnything() throws Exception {
        int generation = (int) (System.currentTimeMillis() / 1000);
        List<GossipDigest> gDigests = new ArrayList<>();
        gDigests.add(new GossipDigest(nodeAddress, generation, 0));

        this.gossiper.liveNodes.add(nodeAddress);
        this.gossiper.endpointStateMap.put(nodeAddress, createEndpointState(generation));

        this.sc.writeInbound(new GossipDigestSyn(gDigests));
        assertTrue(this.sc.finish());

        GossipDigestAck ack = (GossipDigestAck) this.sc.readOutbound();
        assertTrue(ack.getDigest().isEmpty());
        assertTrue(ack.getEndpointStates().isEmpty());
    }

    @Test
    public void testReturnStateOfRestartedNode() throws Exception {
        int generation = (int) (System.currentTimeMillis() / 1000);
        EndpointState endpointState = createEndpointState(generation);
        endpointState.addApplicationState(ApplicationState.SERVICE_NAME, new VersionedValue("Hello", 5));

        this.gossiper.liveNodes.add(nodeAddress);
        this.gossiper.endpointStateMap.put(nodeAddress, endpointState);

        List<GossipDigest> gDigests = new ArrayList<>();
        gDigests.add(new GossipDigest(nodeAddress, generation - 1000, 1000));
        this.sc.writeInbound(new GossipDigestSyn(gDigests));
        assertTrue(this.sc.finish());

        GossipDigestAck ack = (GossipDigestAck) this.sc.readOutbound();
        assertTrue(ack.getDigest().isEmpty());
        assertFalse(ack.getEndpointStates().isEmpty());
        assertEquals(1, ack.getEndpointStates().size());
        assertEquals(endpointState.toString(), ack.getEndpointStates().get(nodeAddress).toString());
    }
    
    @Test
    public void testSendDeltas() throws Exception {
        int generation = (int) (System.currentTimeMillis() / 1000);
        EndpointState endpointState = createEndpointState(generation);
        endpointState.addApplicationState(ApplicationState.SERVICE_NAME, new VersionedValue("Hello", 5));
        
        this.gossiper.liveNodes.add(nodeAddress);
        this.gossiper.endpointStateMap.put(nodeAddress, endpointState);
        
        List<GossipDigest> gDigests = new ArrayList<>();
        gDigests.add(new GossipDigest(nodeAddress, generation, 0));
        this.sc.writeInbound(new GossipDigestSyn(gDigests));
        assertTrue(this.sc.finish());
        
        GossipDigestAck ack = (GossipDigestAck) this.sc.readOutbound();
        assertTrue(ack.getDigest().isEmpty());
        assertFalse(ack.getEndpointStates().isEmpty());
        assertEquals(1, ack.getEndpointStates().size());
        assertEquals(endpointState.toString(), ack.getEndpointStates().get(nodeAddress).toString());
    }

    @After
    public void tearDown() throws Exception {
        sc.close().sync();
    }
}
