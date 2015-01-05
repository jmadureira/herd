package io.herd.netty.codec.thrift;

import static org.junit.Assert.*;
import io.netty.util.IllegalReferenceCountException;

import org.junit.Test;

public class ThriftMessageTest {

    @Test
    public void testGetBuffer() throws Exception {
        String message = "Hello";
        ThriftMessage thriftMessage = new ThriftMessage(ThriftTransportType.FRAMED);
        thriftMessage.write(message.getBytes());
        assertArrayEquals(message.getBytes(), thriftMessage.getBuffer());
    }

    @Test
    public void testCopy() throws Exception {
        String message = "Hello";
        ThriftMessage thriftMessage = new ThriftMessage(ThriftTransportType.FRAMED);
        thriftMessage.write(message.getBytes());
        assertArrayEquals(message.getBytes(), thriftMessage.copy().getBuffer());
    }

    @Test
    public void testOpenClose() throws Exception {
        ThriftMessage thriftMessage = new ThriftMessage(ThriftTransportType.FRAMED);
        assertTrue(thriftMessage.isOpen());
        thriftMessage.close();
        assertFalse(thriftMessage.isOpen());
    }

    @Test(expected = IllegalReferenceCountException.class)
    public void testContentFailDueToClosedContent() throws Exception {
        ThriftMessage thriftMessage = new ThriftMessage(ThriftTransportType.FRAMED);
        assertTrue(thriftMessage.isOpen());
        thriftMessage.close();
        thriftMessage.content();
    }

}
