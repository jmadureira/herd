package io.herd.gossip;

import io.herd.base.Sizes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class GossipDigestAck2 {

    public static final GossipDigestAck2Serializer serializer = new GossipDigestAck2Serializer();

    final Map<InetSocketAddress, EndpointState> nodeStateMap;

    GossipDigestAck2(Map<InetSocketAddress, EndpointState> nodeStateMap) {
        this.nodeStateMap = nodeStateMap;
    }

    Map<InetSocketAddress, EndpointState> getEndpointStates() {
        return nodeStateMap;
    }

    @Override
    public String toString() {
        return "{nodeStateMap: " + nodeStateMap + "}";
    }
}

final class GossipDigestAck2Serializer implements ISerializer<GossipDigestAck2> {

    @Override
    public void serialize(ChannelHandlerContext ctx, GossipDigestAck2 t, ByteBuf out) {
        out.writeByte((byte) 'E');
        out.writeInt(length(t));
        out.writeInt(t.nodeStateMap.size());
        for (Map.Entry<InetSocketAddress, EndpointState> nodeState : t.nodeStateMap.entrySet()) {
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
        Map<InetSocketAddress, EndpointState> nodeStateMap = new HashMap<>();
        for (int i = 0; i < length; i++) {
            try {
                InetSocketAddress endpoint = deserializeSocketAddress(in);
                EndpointState state = EndpointState.serializer.deserialize(ctx, in);
                nodeStateMap.put(endpoint, state);
            } catch (Exception e) {
                throw new CorruptedFrameException("Unable to read node state", e);
            }
        }
        return new GossipDigestAck2(nodeStateMap);
    }

    @Override
    public int length(GossipDigestAck2 t) {
        // the 4 bytes for the nodes state map length
        int size = 4;
        for (Map.Entry<InetSocketAddress, EndpointState> nodeState : t.nodeStateMap.entrySet()) {
            size += Sizes.sizeOf(nodeState.getKey());
            size += EndpointState.serializer.length(nodeState.getValue());
        }
        return size;
    }

}

final class GossipDigestAck2Encoder extends MessageToByteEncoder<GossipDigestAck2> {

    @Override
    protected void encode(ChannelHandlerContext ctx, GossipDigestAck2 msg, ByteBuf out) throws Exception {
        GossipDigestAck2.serializer.serialize(ctx, msg, out);
    }

}
