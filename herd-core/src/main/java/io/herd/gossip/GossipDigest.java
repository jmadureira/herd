package io.herd.gossip;

import static io.herd.base.Sizes.sizeOf;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;

/**
 * A {@link GossipDigest} holds the largest version of the state from a given endpoint know by the local endpoint.
 */
class GossipDigest implements Comparable<GossipDigest> {

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
        return this.generation == other.generation && this.maxVersion == other.maxVersion
                && this.endpoint.equals(other.endpoint);
    }

    @Override
    public int hashCode() {
        int result = 31 * (endpoint == null ? 0 : endpoint.hashCode());
        result = 31 * result + generation;
        result = 31 * result + Long.hashCode(maxVersion);
        return result;
    }

    public String toString() {
        return endpoint + ":" + generation + ":" + maxVersion;
    }

}

final class GossipDigestSerializer implements ISerializer<GossipDigest> {

    @Override
    public void serialize(ChannelHandlerContext ctx, GossipDigest digest, ByteBuf out) {
        serialize(digest.endpoint, out);
        out.writeInt(digest.generation);
        out.writeLong(digest.maxVersion);
    }

    @Override
    public GossipDigest deserialize(ChannelHandlerContext ctx, ByteBuf in) {
        try {
            InetSocketAddress endpoint = deserializeSocketAddress(in);
            int generation = in.readInt();
            long maxVersion = in.readLong();
            return new GossipDigest(endpoint, generation, maxVersion);
        } catch (Exception e) {
            throw new CorruptedFrameException("Failed to deserialize GossipDigest", e);
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
