package io.herd.gossip;

import static io.herd.base.Sizes.sizeOf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * A {@link HeartBeatState} instance simply indicates if something changed on this node.
 * <p/>
 * A heartbeat consists of 2 numbers:
 * <ul>
 * <li>A generation number that remains the same when the node is running and only grows when it is started. Used to
 * distinguish state information before and after a node restarts.</li>
 * <li>A version number grows whenever a node's application state changes and guarantees ordering.</li>
 * </ul>
 * Each node has only one {@link HeartBeatState} associated with it.
 */
class HeartBeatState {

    static final HeartBeatStateSerializer serializer = new HeartBeatStateSerializer();

    final int generation;
    long version;

    HeartBeatState(int generation) {
        this(generation, 0);
    }

    public HeartBeatState(int generation, long version) {
        this.generation = generation;
        this.version = version;
    }

    public String toString() {
        return String.format("{generation=%d, version=%d}", generation, version);
    }

    /**
     * Updates this {@link HeartBeatState} to the latest global version available.
     * 
     * @return The new version of this {@link HeartBeatState}
     */
    long updateVersion() {
        version = VersionGenerator.nextVersion();
        return version;
    }
}

final class HeartBeatStateSerializer implements ISerializer<HeartBeatState> {

    @Override
    public HeartBeatState deserialize(ChannelHandlerContext ctx, ByteBuf in) {

        int generation = in.readInt();
        long version = in.readLong();

        return new HeartBeatState(generation, version);
    }

    @Override
    public int length(HeartBeatState t) {
        return sizeOf(t.generation) + sizeOf(t.version);
    }

    @Override
    public void serialize(ChannelHandlerContext ctx, HeartBeatState t, ByteBuf out) {

        out.writeInt(t.generation);
        out.writeLong(t.version);
    }

}
