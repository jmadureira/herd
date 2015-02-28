package io.herd.base;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Buffers {

    private Buffers() {

    }

    public static final String readString(ByteBuf buffer) {
        return readString(buffer, "UTF-8");
    }

    public static final String readString(ByteBuf buffer, String charset) {
        int readableBytes = buffer.readableBytes();
        ByteArrayOutputStream stream = new ByteArrayOutputStream(readableBytes);
        try {
            buffer.readBytes(stream, readableBytes);
            return stream.toString(charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
