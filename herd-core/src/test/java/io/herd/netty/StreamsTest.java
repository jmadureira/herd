package io.herd.netty;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class StreamsTest {

    @Test
    public void testSmallInputStreamToByteBuf() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("HelloWorld".getBytes());
        ByteBuf buffer = Streams.readToByteBuf(inputStream);
        byte[] array = new byte[buffer.readableBytes()];
        buffer.readBytes(array);
        Assert.assertEquals("HelloWorld", new String(array));
    }
    
    @Test
    public void testMediumInputStreamToByteBuf() throws IOException {
        StringBuilder builder = new StringBuilder(2056);
        for(int i = 0; i < 100; i++) {
            builder.append("HelloWorld" + i + "->");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(builder.toString().getBytes());
        ByteBuf buffer = Streams.readToByteBuf(inputStream);
        byte[] array = new byte[buffer.readableBytes()];
        buffer.readBytes(array);
        Assert.assertEquals(builder.toString(), new String(array));
    }
    
    @Test
    public void testBigInputStreamToByteBuf() throws IOException {
        StringBuilder builder = new StringBuilder(2056);
        for(int i = 0; i < 400; i++) {
            builder.append("HelloWorld" + i + "->");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(builder.toString().getBytes());
        ByteBuf buffer = Streams.readToByteBuf(inputStream);
        byte[] array = new byte[buffer.readableBytes()];
        buffer.readBytes(array);
        Assert.assertEquals(builder.toString(), new String(array));
    }
    
}
