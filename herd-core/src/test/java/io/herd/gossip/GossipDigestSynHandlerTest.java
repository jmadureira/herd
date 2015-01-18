package io.herd.gossip;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.*;

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

public class GossipDigestSynHandlerTest {

    private static EventLoopGroup group;

    private EmbeddedChannel sc;
    private Gossiper gossiper;

    private InetSocketAddress localhost;

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
        this.sc = new EmbeddedChannel(new GossipDigestSynHandler(gossiper));
    }

    @Test
    public void testAskForUnknwonNode() throws Exception {
        int generation = (int) (System.currentTimeMillis() / 1000);
        InetSocketAddress node = new InetSocketAddress(InetAddress.getByName("www.google.com"), 8080);
        List<GossipDigest> gDigests = new ArrayList<>();
        gDigests.add(new GossipDigest(node, generation, 0));

        this.sc.writeInbound(new GossipDigestSyn(gDigests));
        assertTrue(this.sc.finish());

        GossipDigestAck ack = (GossipDigestAck) this.sc.readOutbound();
        assertEquals(gDigests, ack.digests);
        assertTrue(ack.getEndpointStates().isEmpty());
    }
    
    @Test
    public void testAskForRestartedNode() throws Exception {
        int generation = (int) (System.currentTimeMillis() / 1000);
        InetSocketAddress node = new InetSocketAddress(InetAddress.getByName("www.google.com"), 8080);
        List<GossipDigest> gDigests = new ArrayList<>();
        gDigests.add(new GossipDigest(node, generation + 1000, 0));
        
        this.gossiper.liveNodes.add(node);
        this.gossiper.endpointStateMap.put(node, new EndpointState(new HeartBeatState(generation, 0)));
        
        this.sc.writeInbound(new GossipDigestSyn(gDigests));
        assertTrue(this.sc.finish());
        
        GossipDigestAck ack = (GossipDigestAck) this.sc.readOutbound();
        assertEquals(gDigests, ack.digests);
        assertTrue(ack.getEndpointStates().isEmpty());
    }
    
    @Test
    public void testDoNotAskForAnything() throws Exception {
        int generation = (int) (System.currentTimeMillis() / 1000);
        InetSocketAddress node = new InetSocketAddress(InetAddress.getByName("www.google.com"), 8080);
        List<GossipDigest> gDigests = new ArrayList<>();
        gDigests.add(new GossipDigest(node, generation, 0));
        
        this.gossiper.liveNodes.add(node);
        this.gossiper.endpointStateMap.put(node, new EndpointState(new HeartBeatState(generation, 0)));
        
        this.sc.writeInbound(new GossipDigestSyn(gDigests));
        assertTrue(this.sc.finish());
        
        GossipDigestAck ack = (GossipDigestAck) this.sc.readOutbound();
        assertTrue(ack.getDigest().isEmpty());
        assertTrue(ack.getEndpointStates().isEmpty());
    }
    
    @Test
    public void testDoNotAskForOldNode() throws Exception {
        int generation = (int) (System.currentTimeMillis() / 1000);
        InetSocketAddress node = new InetSocketAddress(InetAddress.getByName("www.google.com"), 8080);
        List<GossipDigest> gDigests = new ArrayList<>();
        gDigests.add(new GossipDigest(node, generation - 1000, 0));
        
        this.gossiper.liveNodes.add(node);
        this.gossiper.endpointStateMap.put(node, new EndpointState(new HeartBeatState(generation, 0)));
        
        this.sc.writeInbound(new GossipDigestSyn(gDigests));
        assertTrue(this.sc.finish());
        
        GossipDigestAck ack = (GossipDigestAck) this.sc.readOutbound();
        assertTrue(ack.getDigest().isEmpty());
        assertFalse(ack.getEndpointStates().isEmpty());
    }

    @After
    public void tearDown() throws Exception {
        sc.close().sync();
    }
}
