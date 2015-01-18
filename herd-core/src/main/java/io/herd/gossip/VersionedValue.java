package io.herd.gossip;

import static io.herd.base.Preconditions.checkNotNull;
import static io.herd.base.Sizes.sizeOf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class VersionedValue implements Comparable<VersionedValue> {
    
    static final VersionedValueSerializer serializer = new VersionedValueSerializer();

    private final String value;
    private final long version;

    public VersionedValue(String value) {
        this(value, VersionGenerator.nextVersion());
    }

    public VersionedValue(String value, long version) {
        this.value = checkNotNull(value, "Cannot create a versioned value from a null string");
        this.version = version;
    }

    public String getValue() {
        return value;
    }

    public long getVersion() {
        return version;
    }

    /**
     * Comparing {@link VersionedValue} involves mainly it's version. This is because it doesn't make any sense to
     * compare version of different values.
     */
    @Override
    public int compareTo(VersionedValue o) {
        return (int) (version - o.version);
    }

    @Override
    public String toString() {
        return "{value=" + value + ", version=" + version + "}";
    }

}

final class VersionedValueSerializer implements ISerializer<VersionedValue> {

    @Override
    public VersionedValue deserialize(ChannelHandlerContext ctx, ByteBuf in) {

        String value = deserializeString(in);
        long version = in.readLong();

        return new VersionedValue(value, version);
    }

    @Override
    public int length(VersionedValue t) {
        return sizeOf(t.getValue()) + sizeOf(t.getVersion());
    }

    @Override
    public void serialize(ChannelHandlerContext ctx, VersionedValue t, ByteBuf out) {
        
        serialize(t.getValue(), out);
        out.writeLong(t.getVersion());
    }

}
