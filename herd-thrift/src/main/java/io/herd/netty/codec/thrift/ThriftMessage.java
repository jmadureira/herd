package io.herd.netty.codec.thrift;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.util.IllegalReferenceCountException;

public class ThriftMessage extends TTransport implements ByteBufHolder {

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private final ByteBuf payload;
    private final ThriftTransportType transportType;

    public ThriftMessage(ThriftTransportType transportType, ByteBuf payload) {
        this.payload = payload;
        this.transportType = transportType;
    }

    public ThriftMessage(ThriftTransportType transportType) {
        this(transportType, Unpooled.directBuffer(DEFAULT_BUFFER_SIZE));
    }

    @Override
    public void write(byte[] array, int offset, int length) throws TTransportException {
        content().writeBytes(array, offset, length);
    }

    @Override
    public int read(byte[] array, int offset, int length) throws TTransportException {
        int bytesToRead = Math.min(content().readableBytes(), length);
        if (bytesToRead > 0) {
            content().readBytes(array, offset, bytesToRead);
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
        content().release();
    }

    @Override
    public int refCnt() {
        return content().refCnt();
    }

    @Override
    public boolean release() {
        return content().release();
    }

    @Override
    public boolean release(int decrement) {
        return content().release(decrement);
    }

    @Override
    public ByteBuf content() {
        if (payload.refCnt() <= 0) {
            throw new IllegalReferenceCountException(payload.refCnt());
        }
        return payload;
    }

    @Override
    public ThriftMessage copy() {
        return new ThriftMessage(transportType, payload.copy());
    }

    @Override
    public ThriftMessage duplicate() {
        return new ThriftMessage(transportType, payload.duplicate());
    }

    @Override
    public ThriftMessage retain() {
        content().retain();
        return this;
    }

    @Override
    public ThriftMessage retain(int increment) {
        content().retain(increment);
        return this;
    }

    public ThriftTransportType getTransportType() {
        return transportType;
    }

}
