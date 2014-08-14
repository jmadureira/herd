package io.herd.netty.codec.thrift;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.util.IllegalReferenceCountException;

public class ThriftMessage implements ByteBufHolder {

    private final ByteBuf payload;
    private final ThriftTransportType transportType;

    public ThriftMessage(ThriftTransportType transportType, ByteBuf payload) {
        this.payload = payload;
        this.transportType = transportType;
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

}
