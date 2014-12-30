package io.herd.gossip;

import java.io.IOException;
import java.net.InetAddress;

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

    default InetAddress deserializeAddress(ByteBuf buf) throws IOException {
        int addressLength = buf.readByte();
        byte[] addressArray = new byte[addressLength];
        buf.readBytes(addressArray);
        return InetAddress.getByAddress(addressArray);
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
}
