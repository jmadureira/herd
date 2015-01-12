package io.herd.gossip;

import static org.junit.Assert.assertEquals;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;

public class GossipDigestAckTest {

    @Test
    public void testEncodingDecodingEmptyMessage() throws Exception {
        
        GossipDigestAckEncoder encoder = new GossipDigestAckEncoder();
        GossipMessageDecoder decoder = new GossipMessageDecoder();
        
        List<GossipDigest> digestList = new ArrayList<>();
        Map<InetSocketAddress, EndpointState> nodeStateMap = new HashMap<>();
        ByteBuf buffer = Unpooled.buffer();
        GossipDigestAck msg = new GossipDigestAck(digestList, nodeStateMap);
        
        List<Object> out = Lists.newArrayList();
        encoder.encode(null, msg, buffer);
        decoder.decode(null, buffer, out);
        
        assertEquals(1, out.size());
        GossipDigestAck result = (GossipDigestAck) out.get(0);
        
        assertEquals(digestList, result.getDigest());
    }
    
    @Test
    public void testEncodingDecodingMessage() throws Exception {
        
        GossipDigestAckEncoder encoder = new GossipDigestAckEncoder();
        GossipMessageDecoder decoder = new GossipMessageDecoder();
        
        List<GossipDigest> digestList = new ArrayList<>();
        digestList.add(new GossipDigest(new InetSocketAddress(InetAddress.getLocalHost(), 8080), 4, 324L));
        
        Map<InetSocketAddress, EndpointState> nodeStateMap = new HashMap<>();
        HeartBeatState hbState = new HeartBeatState(5, 1234L);
        EndpointState state = new EndpointState(hbState);
        nodeStateMap.put(new InetSocketAddress(InetAddress.getLocalHost(), 8080), state);
        
        ByteBuf buffer = Unpooled.buffer();
        GossipDigestAck msg = new GossipDigestAck(digestList, nodeStateMap);
        
        List<Object> out = Lists.newArrayList();
        encoder.encode(null, msg, buffer);
        decoder.decode(null, buffer, out);
        
        assertEquals(1, out.size());
        GossipDigestAck result = (GossipDigestAck) out.get(0);
        
        assertEquals(1, result.getDigest().size());
        GossipDigest digest = result.getDigest().get(0);
        assertEquals(new InetSocketAddress(InetAddress.getLocalHost(), 8080), digest.endpoint);
        assertEquals(4, digest.generation);
        assertEquals(324L, digest.maxVersion);
        
        assertEquals(1, result.getEndpointStates().size());
        EndpointState endpointState = result.getEndpointStates().get(new InetSocketAddress(InetAddress.getLocalHost(), 8080));
        assertEquals(5, endpointState.getHeartBeatState().generation);
        assertEquals(1234L, endpointState.getHeartBeatState().version);
    }
}
