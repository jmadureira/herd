package io.herd.gossip;

import static io.herd.base.Sizes.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link GossipDigest} holds the largest version of the state from a given endpoint know by the local endpoint.
 */
public class GossipDigest implements Comparable<GossipDigest> {

    public static final ISerializer<GossipDigest> serializer = new GossipDigestSerializer();

    final InetSocketAddress endpoint;
    final int generation;
    final long maxVersion;

    public GossipDigest(InetSocketAddress endpoint, int generation, long maxVersion) {
        this.endpoint = endpoint;
        this.generation = generation;
        this.maxVersion = maxVersion;
    }

    /**
     * On {@link GossipDigest} comparison we're mainly interested in comparing versions so we ignore the actual address.
     */
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
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GossipDigest)) {
            return false;
        }
        
        if (this == obj) {
            return true;
        }

        GossipDigest other = (GossipDigest) obj;
        return this.generation == other.generation
                && this.maxVersion == other.maxVersion
                && this.endpoint.equals(other.endpoint);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(endpoint, generation, maxVersion);
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
            int port = in.readShort();
            int addressLength = in.readByte();
            byte[] addressArray = new byte[addressLength];
            in.readBytes(addressArray);
            InetAddress endpoint = InetAddress.getByAddress(addressArray);
            int generation = in.readInt();
            long maxVersion = in.readLong();
            return new GossipDigest(new InetSocketAddress(endpoint, port), generation, maxVersion);
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
