package io.herd.thrift;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * A {@link TDirectByteBufTransport} acts as a wrapper for an incoming {@link ByteBuf} into a {@link TTransport}. What
 * this class has in particular is that it is designed to work with {@link ByteBuf} not backed by an array.
 * 
 * @author joaomadureira
 *
 */
public class TDirectByteBufTransport extends TTransport implements TByteBufTransport {

    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private final ByteBuf inBuffer;
    private final ByteBuf outBuffer;

    public TDirectByteBufTransport(ByteBuf buf) {
        super();
        this.inBuffer = buf;
        /*
         * TODO we might not need to instantiate a direct buffer.
         */
        this.outBuffer = Unpooled.directBuffer(DEFAULT_BUFFER_SIZE);
    }

    @Override
    public void write(byte[] array, int offset, int length) throws TTransportException {
        outBuffer.writeBytes(array, offset, length);
    }

    @Override
    public int read(byte[] array, int offset, int length) throws TTransportException {
        int bytesToRead = Math.min(inBuffer.readableBytes(), length);
        if (bytesToRead > 0) {
            inBuffer.readBytes(array, offset, bytesToRead);
        }
        return bytesToRead;
    }

    @Override
    public void open() throws TTransportException {
        // there's nothing to do here
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() {
        this.outBuffer.release();
    }

    @Override
    public ByteBuf getOutputBuffer() {
        return outBuffer;
    }
}
