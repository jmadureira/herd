package io.herd.gossip;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * First message that gets sent out at the start of a gossip protocol round.
 * 
 */
public class GossipDigestSyn {

    static final GossipDigestSynSerializer serializer = new GossipDigestSynSerializer();

    final List<GossipDigest> digests;

    public GossipDigestSyn(List<GossipDigest> digests) {
        this.digests = digests;
    }

    List<GossipDigest> getDigest() {
        return digests;
    }

    @Override
    public String toString() {
        return "{digests: " + digests + "}";
    }

}

final class GossipDigestSynSerializer implements ISerializer<GossipDigestSyn> {
    
    @Override
    public void serialize(ChannelHandlerContext ctx, GossipDigestSyn t, ByteBuf out) {
        
        out.writeByte((byte) 'S');
        out.writeInt(length(t));
        out.writeInt(t.digests.size());
        for (GossipDigest digest : t.digests) {
            GossipDigest.serializer.serialize(ctx, digest, out);
        }
    }

    @Override
    public GossipDigestSyn deserialize(ChannelHandlerContext ctx, ByteBuf in) {

        int payloadSize = in.readInt();
        if(in.readableBytes() < payloadSize) {
            return null;
        }
        int length = in.readInt();
        List<GossipDigest> digests = new ArrayList<GossipDigest>(length);
        for (int i = 0; i < length; i++) {
            digests.add(GossipDigest.serializer.deserialize(ctx, in));
        }
        return new GossipDigestSyn(digests);
    }

    @Override
    public int length(GossipDigestSyn t) {
        // the size 4 is for the length of the digest array
        int size = 4;
        for(GossipDigest digest : t.digests) {
            size += GossipDigest.serializer.length(digest);
        }
        return size;
    }
    
}

final class GossipDigestSynEncoder extends MessageToByteEncoder<GossipDigestSyn> {

    @Override
    protected void encode(ChannelHandlerContext ctx, GossipDigestSyn msg, ByteBuf out) throws Exception {
        GossipDigestSyn.serializer.serialize(ctx, msg, out);
    }

}

