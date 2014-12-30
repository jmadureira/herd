package io.herd.gossip;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * First message that gets sent out at the start of a gossip protocol round.
 * 
 * @author joaomadureira
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
        return new StringBuilder("GossipDigestSyn:")
                .append(digests)
                .toString();
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

    private static final Logger logger = LoggerFactory.getLogger(GossipDigestSynEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, GossipDigestSyn msg, ByteBuf out) throws Exception {
        
        logger.debug("Encoding {}", msg);

        GossipDigestSyn.serializer.serialize(ctx, msg, out);
    }

}

