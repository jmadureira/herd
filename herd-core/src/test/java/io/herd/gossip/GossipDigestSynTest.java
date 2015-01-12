package io.herd.gossip;

import static org.junit.Assert.assertEquals;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class GossipDigestSynTest {

    @Test
    public void testEncodingDecodingEmptyMessage() throws Exception {
        
        GossipDigestSynEncoder encoder = new GossipDigestSynEncoder();
        GossipMessageDecoder decoder = new GossipMessageDecoder();
        
        List<GossipDigest> digestList = new ArrayList<>();
        ByteBuf buffer = Unpooled.buffer();
        GossipDigestSyn msg = new GossipDigestSyn(digestList);
        
        List<Object> out = Lists.newArrayList();
        encoder.encode(null, msg, buffer);
        decoder.decode(null, buffer, out);
        
        assertEquals(1, out.size());
        GossipDigestSyn result = (GossipDigestSyn) out.get(0);
        
        assertEquals(digestList, result.getDigest());
    }
    
    @Test
    public void testEncodingDecodingMessage() throws Exception {
        
        GossipDigestSynEncoder encoder = new GossipDigestSynEncoder();
        GossipMessageDecoder decoder = new GossipMessageDecoder();
        
        List<GossipDigest> digestList = new ArrayList<>();
        digestList.add(new GossipDigest(new InetSocketAddress(InetAddress.getLocalHost(), 8080), 4, 324L));
        ByteBuf buffer = Unpooled.buffer();
        GossipDigestSyn msg = new GossipDigestSyn(digestList);
        
        List<Object> out = Lists.newArrayList();
        encoder.encode(null, msg, buffer);
        decoder.decode(null, buffer, out);
        
        assertEquals(1, out.size());
        GossipDigestSyn result = (GossipDigestSyn) out.get(0);
        
        assertEquals(1, result.getDigest().size());
        GossipDigest digest = result.getDigest().get(0);
        assertEquals(new InetSocketAddress(InetAddress.getLocalHost(), 8080), digest.endpoint);
        assertEquals(4, digest.generation);
        assertEquals(324L, digest.maxVersion);
    }
}
