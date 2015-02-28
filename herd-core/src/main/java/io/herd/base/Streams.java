package io.herd.base;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.io.InputStream;

public final class Streams {
    
    private Streams() {
        
    }
    
    public static final ByteBuf readToByteBuf(InputStream inputStream) throws IOException {
        return readToByteBuf(inputStream, Unpooled.buffer());
    }

    public static final ByteBuf readToByteBuf(InputStream inputStream, ByteBuf byteBuf) throws IOException {

        while(byteBuf.writeBytes(inputStream, 4096) >= 4096) {
            
        }
        return byteBuf;
    }
}
