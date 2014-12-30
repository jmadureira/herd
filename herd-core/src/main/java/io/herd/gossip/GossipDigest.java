package io.herd.gossip;

import static io.herd.base.Sizes.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link GossipDigest} holds the largest version of the state from a given endpoint know by the local endpoint.
 */
public class GossipDigest implements Comparable<GossipDigest> {

    public static final ISerializer<GossipDigest> serializer = new GossipDigestSerializer();

    final InetAddress endpoint;
    final int generation;
    final long maxVersion;

    public GossipDigest(InetAddress endpoint, int generation, long maxVersion) {
        this.endpoint = endpoint;
        this.generation = generation;
        this.maxVersion = maxVersion;
    }

    @Override
    public int compareTo(GossipDigest gDigest) {
        if (generation != gDigest.generation) {
            return generation - gDigest.generation;
        }
        /*
         * it will be unlikely to reach a scenario where the version we have is so old that the difference between the
         * latest version and the one we have is higher than Integer.MAX_VALUE.
         */
        return (int) (maxVersion - gDigest.maxVersion);
    }

    public String toString() {
        return new StringBuilder()
                .append(endpoint)
                .append(':')
                .append(generation)
                .append(':')
                .append(maxVersion)
                .toString();
    }

}

final class GossipDigestSerializer implements ISerializer<GossipDigest> {

    private static final Logger logger = LoggerFactory.getLogger(GossipDigestSerializer.class);

    @Override
    public void serialize(ChannelHandlerContext ctx, GossipDigest digest, ByteBuf out) {
        serialize(digest.endpoint, out);
        out.writeInt(digest.generation);
        out.writeLong(digest.maxVersion);
    }

    @Override
    public GossipDigest deserialize(ChannelHandlerContext ctx, ByteBuf in) {
        try {
            int addressLength = in.readByte();
            byte[] addressArray = new byte[addressLength];
            in.readBytes(addressArray);
            InetAddress endpoint = InetAddress.getByAddress(addressArray);
            int generation = in.readInt();
            long maxVersion = in.readLong();
            return new GossipDigest(endpoint, generation, maxVersion);
        } catch (Exception e) {
            logger.error("Failed to deserialize GossipDigest due to {}.", e.toString());
            return null;
        }
    }

    @Override
    public int length(GossipDigest t) {
        /*
         * the 1 is the short size needed to encode the size of the address byte array
         */
        return 1 + sizeOf(t.endpoint) + sizeOf(t.generation) + sizeOf(t.maxVersion);
    }

}