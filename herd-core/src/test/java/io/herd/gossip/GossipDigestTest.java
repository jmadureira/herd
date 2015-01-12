package io.herd.gossip;

import static org.junit.Assert.assertEquals;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.Test;

public class GossipDigestTest {

    @Test
    public void testSerializeDeserialize() throws Exception {
        GossipDigest gDigest = new GossipDigest(new InetSocketAddress(InetAddress.getLocalHost(), 8080), 4, 2341234L);
        ByteBuf buffer = Unpooled.buffer();
        GossipDigest.serializer.serialize(null, gDigest, buffer);
        GossipDigest newDigest = GossipDigest.serializer.deserialize(null, buffer);
        assertEquals(gDigest.endpoint, newDigest.endpoint);
        assertEquals(gDigest.generation, newDigest.generation);
        assertEquals(gDigest.maxVersion, newDigest.maxVersion);
    }
}
