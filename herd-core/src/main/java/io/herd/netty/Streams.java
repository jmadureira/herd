package io.herd.netty;

import java.io.IOException;
import java.io.InputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

public final class Streams {

    public static final ByteBuf readToByteBuf(InputStream inputStream) throws IOException {
        return readToByteBuf(inputStream, Unpooled.buffer());
    }

    public static final ByteBuf readToByteBuf(InputStream inputStream, ByteBuf byteBuf) throws IOException {

        ByteBufOutputStream outStream = new ByteBufOutputStream(byteBuf);

        byte[] array = new byte[4096];
        int bytesRead = 0;
        while ((bytesRead = inputStream.read(array)) != -1) {
            outStream.write(array, 0, bytesRead);
        }
        inputStream.close();
        // we close the output stream just because since it doesn't really do anything
        outStream.close();
        return byteBuf;
    }
}
