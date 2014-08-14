package io.herd.thrift;

import io.netty.buffer.ByteBuf;

public interface TByteBufTransport {

    /**
     * @return The {@link ByteBuf} where the output was serialized into.
     */
    ByteBuf getOutputBuffer();
}
