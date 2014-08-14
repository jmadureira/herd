package io.herd.thrift;

import org.apache.thrift.transport.TTransport;

import io.netty.buffer.ByteBuf;

public class TTransports {

    public static final TTransport getTransport(ByteBuf buffer) {
        /*
         * TODO bytebufs backed by an array can use a specific transport that accesses that array directly but for now
         * simply use the direct transport for everything.
         */
        return buffer.hasArray() ? new TDirectByteBufTransport(buffer) : new TDirectByteBufTransport(buffer);
    }
}
