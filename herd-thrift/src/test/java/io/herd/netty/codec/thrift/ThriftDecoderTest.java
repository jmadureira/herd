package io.herd.netty.codec.thrift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.CharsetUtil;

import org.junit.Before;
import org.junit.Test;

public class ThriftDecoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        this.channel = new EmbeddedChannel(new ThriftDecoder(50));
    }

    @Test
    public void testFrameOverflow() throws Exception {
        ByteBuf input = Unpooled.buffer(25);
        String content = "some random content I've just added here that is too long for the frame";
        input.writeInt(content.length());
        input.writeBytes(content.getBytes());
        try {
            this.channel.writeInbound(input);
            fail("Should have thrown a frame too long exception");
        } catch (TooLongFrameException e) {

        }
    }
    
    @Test
    public void testFramedDecoding() throws Exception {
        ByteBuf input = Unpooled.buffer(25);
        String content = "some random content I've just added here";
        input.writeInt(content.length());
        input.writeBytes(content.getBytes());

        this.channel.writeInbound(input);
        
        ThriftMessage object = (ThriftMessage) this.channel.readInbound();
        assertEquals(content, object.content().toString(CharsetUtil.UTF_8));
    }
    
}
