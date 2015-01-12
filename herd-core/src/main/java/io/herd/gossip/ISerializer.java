package io.herd.gossip;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Interface definition for implementations capable of serializing/deserializing objects of type <code>T</code>
 * into/from {@link ByteBuf}s.
 *
 * 
 * @param <T> The type of objects that are going to be serialized/deserialized.
 */
interface ISerializer<T> {

    T deserialize(ChannelHandlerContext ctx, ByteBuf in);

    /**
     * Reads an {@link InetAddress} from a {@link ByteBuf}. No boundary checks are made here.
     * 
     * @param buf A {@link ByteBuf} with enough readable bytes to read an {@link InetAddress}.
     * @return An {@link InetAddress}. Null is never returned.
     * @throws IOException If unable to create an {@link InetAddress} from the information taken from the
     *             {@link ByteBuf}
     */
    default InetAddress deserializeAddress(ByteBuf buf) throws IOException {
        int addressLength = buf.readByte();
        byte[] addressArray = new byte[addressLength];
        buf.readBytes(addressArray);
        return InetAddress.getByAddress(addressArray);
    }

    /**
     * Reads an {@link InetSocketAddress} from a {@link ByteBuf}. No boundary checks are made here.
     * 
     * @param buf A {@link ByteBuf} with enough readable bytes to read an {@link InetSocketAddress}.
     * @return An {@link InetSocketAddress}. Null is never returned.
     * @throws IOException If unable to create an {@link InetSocketAddress} from the information taken from the
     *             {@link ByteBuf}
     */
    default InetSocketAddress deserializeSocketAddress(ByteBuf buf) throws IOException {
        int port = buf.readShort();
        return new InetSocketAddress(deserializeAddress(buf), port);
    }

    /**
     * @return The expected length in bytes.
     */
    int length(T t);

    void serialize(ChannelHandlerContext ctx, T t, ByteBuf out);

    default void serialize(InetAddress address, ByteBuf buf) {
        byte[] array = address.getAddress();
        buf.writeByte(array.length);
        buf.writeBytes(array);
    }

    default void serialize(InetSocketAddress address, ByteBuf buf) {
        buf.writeShort(address.getPort());
        serialize(address.getAddress(), buf);
    }
}
