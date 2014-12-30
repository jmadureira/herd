package io.herd.gossip;

import io.herd.base.Sizes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class GossipDigestAck {

    static final GossipDigestAckSerializer serializer = new GossipDigestAckSerializer();

    final List<GossipDigest> digests;
    final Map<InetAddress, EndpointState> nodeStateMap;

    GossipDigestAck(List<GossipDigest> digests, Map<InetAddress, EndpointState> nodeStateMap) {
        this.digests = digests;
        this.nodeStateMap = nodeStateMap;
    }

    List<GossipDigest> getDigest() {
        return digests;
    }

    Map<InetAddress, EndpointState> getEndpointStates() {
        return nodeStateMap;
    }

    @Override
    public String toString() {
        return new StringBuilder("GossipDigestAck: ")
                .append(digests)
                .append(nodeStateMap)
                .toString();
    }

}

final class GossipDigestAckSerializer implements ISerializer<GossipDigestAck> {
    
    private static final Logger logger = LoggerFactory.getLogger(GossipDigestAckSerializer.class);

    @Override
    public void serialize(ChannelHandlerContext ctx, GossipDigestAck t, ByteBuf out) {
        out.writeByte((byte) 'A');
        out.writeInt(length(t));
        out.writeInt(t.digests.size());
        for (GossipDigest digest : t.digests) {
            GossipDigest.serializer.serialize(ctx, digest, out);
        }
        out.writeInt(t.nodeStateMap.size());
        for (Map.Entry<InetAddress, EndpointState> nodeState : t.nodeStateMap.entrySet()) {
            serialize(nodeState.getKey(), out);
            EndpointState.serializer.serialize(ctx, nodeState.getValue(), out);
        }
    }

    @Override
    public GossipDigestAck deserialize(ChannelHandlerContext ctx, ByteBuf in) {

        int payloadSize = in.readInt();
        if (in.readableBytes() < payloadSize) {
            logger.debug("Not enough bytes yet. Expected {} but still has {}", payloadSize, in.readableBytes());
            return null;
        }
        int length = in.readInt();
        List<GossipDigest> digests = new ArrayList<GossipDigest>(length);
        for (int i = 0; i < length; i++) {
            digests.add(GossipDigest.serializer.deserialize(ctx, in));
        }
        length = in.readInt();
        Map<InetAddress, EndpointState> nodeStateMap = Maps.newHashMap();
        for (int i = 0; i < length; i++) {
            try {
                InetAddress endpoint = deserializeAddress(in);
                EndpointState state = EndpointState.serializer.deserialize(ctx, in);
                nodeStateMap.put(endpoint, state);
            } catch (Exception e) {
                throw new CorruptedFrameException("Unable to read node state due to " + e.toString());
            }
        }
        return new GossipDigestAck(digests, nodeStateMap);
    }

    @Override
    public int length(GossipDigestAck t) {
        // the size 4 is for the length of the digest array
        int size = 4;
        for (GossipDigest digest : t.digests) {
            size += GossipDigest.serializer.length(digest);
        }
        // another 4 bytes for the nodes tate map length
        size += 4;
        for (Map.Entry<InetAddress, EndpointState> nodeState : t.nodeStateMap.entrySet()) {
            size += Sizes.sizeOf(nodeState.getKey());
            size += EndpointState.serializer.length(nodeState.getValue());
        }
        return size;
    }

}

final class GossipDigestAckEncoder extends MessageToByteEncoder<GossipDigestAck> {

    private static final Logger logger = LoggerFactory.getLogger(GossipDigestAckEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, GossipDigestAck msg, ByteBuf out) throws Exception {

        logger.debug("Encoding {}", msg);

        GossipDigestAck.serializer.serialize(ctx, msg, out);
    }
    
}

