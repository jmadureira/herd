package io.herd.gossip;

import static org.junit.Assert.assertEquals;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;

public class GossipDigestAck2Test {

    @Test
    public void testEncodingDecodingEmptyMessage() throws Exception {
        
        GossipDigestAck2Encoder encoder = new GossipDigestAck2Encoder();
        GossipMessageDecoder decoder = new GossipMessageDecoder();
        
        Map<InetSocketAddress, EndpointState> nodeStateMap = new HashMap<>();
        ByteBuf buffer = Unpooled.buffer();
        GossipDigestAck2 msg = new GossipDigestAck2(nodeStateMap);
        
        List<Object> out = Lists.newArrayList();
        encoder.encode(null, msg, buffer);
        decoder.decode(null, buffer, out);
        
        assertEquals(1, out.size());
        GossipDigestAck2 result = (GossipDigestAck2) out.get(0);
        
        assertEquals(nodeStateMap, result.getEndpointStates());
    }
    
    @Test
    public void testEncodingDecodingMessage() throws Exception {
        
        GossipDigestAck2Encoder encoder = new GossipDigestAck2Encoder();
        GossipMessageDecoder decoder = new GossipMessageDecoder();
        
        Map<InetSocketAddress, EndpointState> nodeStateMap = new HashMap<>();
        HeartBeatState hbState = new HeartBeatState(5, 1234L);
        EndpointState state = new EndpointState(hbState);
        nodeStateMap.put(new InetSocketAddress(InetAddress.getLocalHost(), 8080), state);
        
        ByteBuf buffer = Unpooled.buffer();
        GossipDigestAck2 msg = new GossipDigestAck2(nodeStateMap);
        
        List<Object> out = Lists.newArrayList();
        encoder.encode(null, msg, buffer);
        decoder.decode(null, buffer, out);
        
        assertEquals(1, out.size());
        GossipDigestAck2 result = (GossipDigestAck2) out.get(0);
        
        assertEquals(1, result.getEndpointStates().size());
        EndpointState endpointState = result.getEndpointStates().get(new InetSocketAddress(InetAddress.getLocalHost(), 8080));
        assertEquals(5, endpointState.getHeartBeatState().generation);
        assertEquals(1234L, endpointState.getHeartBeatState().version);
    }
}
