package io.herd.gossip;

import io.herd.base.Sizes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;

import java.net.InetAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class GossipDigestAck2 {

    public static final GossipDigestAck2Serializer serializer = new GossipDigestAck2Serializer();

    final Map<InetAddress, EndpointState> nodeStateMap;

    GossipDigestAck2(Map<InetAddress, EndpointState> nodeStateMap) {
        this.nodeStateMap = nodeStateMap;
    }
    
    Map<InetAddress, EndpointState> getEndpointStates() {
        return nodeStateMap;
    }
    
    @Override
    public String toString() {
        return new StringBuilder("GossipDigestAck2: ")
                .append(nodeStateMap)
                .toString();
    }
}

final class GossipDigestAck2Serializer implements ISerializer<GossipDigestAck2> {

    @Override
    public void serialize(ChannelHandlerContext ctx, GossipDigestAck2 t, ByteBuf out) {
        out.writeByte((byte) 'E');
        out.writeInt(length(t));
        out.writeInt(t.nodeStateMap.size());
        for (Map.Entry<InetAddress, EndpointState> nodeState : t.nodeStateMap.entrySet()) {
            serialize(nodeState.getKey(), out);
            EndpointState.serializer.serialize(ctx, nodeState.getValue(), out);
        }

    }

    @Override
    public GossipDigestAck2 deserialize(ChannelHandlerContext ctx, ByteBuf in) {

        int payloadSize = in.readInt();
        if (in.readableBytes() < payloadSize) {
            return null;
        }
        int length = in.readInt();
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
        return new GossipDigestAck2(nodeStateMap);
    }

    @Override
    public int length(GossipDigestAck2 t) {
        // the 4 bytes for the nodes state map length
        int size = 4;
        for (Map.Entry<InetAddress, EndpointState> nodeState : t.nodeStateMap.entrySet()) {
            size += Sizes.sizeOf(nodeState.getKey());
            size += EndpointState.serializer.length(nodeState.getValue());
        }
        return size;
    }

}

final class GossipDigestAck2Encoder extends MessageToByteEncoder<GossipDigestAck2> {

    private static final Logger logger = LoggerFactory.getLogger(GossipDigestAck2Encoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, GossipDigestAck2 msg, ByteBuf out) throws Exception {

        logger.debug("Encoding {}", msg);

        GossipDigestAck2.serializer.serialize(ctx, msg, out);
    }

}
