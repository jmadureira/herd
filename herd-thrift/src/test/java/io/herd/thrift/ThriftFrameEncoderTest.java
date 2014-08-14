package io.herd.thrift;

import io.herd.netty.codec.thrift.ThriftEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.CharsetUtil;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ThriftFrameEncoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        this.channel = new EmbeddedChannel(new ThriftEncoder(50));
    }

    @Test
    public void testFrameOverflow() throws Exception {
        ByteBuf input = Unpooled.buffer(25);
        String content = "some random content I've just added here that is too long for the frame";
        TDirectByteBufTransport transport = new TDirectByteBufTransport(input);
        transport.write(content.getBytes(), 0, content.length());
        try {
            this.channel.writeOutbound(transport);
            fail("Should have thrown a frame too long exception");
        } catch (TooLongFrameException e) {

        }
    }

    @Test
    public void testFramedEncoding() throws Exception {
        ByteBuf input = Unpooled.buffer(25);
        String content = "some random content I've just added here";
        TDirectByteBufTransport transport = new TDirectByteBufTransport(input);
        transport.write(content.getBytes(), 0, content.length());
        this.channel.writeOutbound(transport);

        ByteBuf object = (ByteBuf) this.channel.readOutbound();
        assertEquals(content.length(), object.readInt());
        assertEquals(content, object.toString(CharsetUtil.UTF_8));
    }
}
