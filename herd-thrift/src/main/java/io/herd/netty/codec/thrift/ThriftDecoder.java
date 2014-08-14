package io.herd.netty.codec.thrift;

import io.herd.netty.codec.thrift.ThriftMessage;
import io.herd.netty.codec.thrift.ThriftTransportType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;

import javax.naming.OperationNotSupportedException;

class ThriftDecoder extends ByteToMessageDecoder {

    public static final int MESSAGE_FRAME_SIZE = 4;
    private final int maxFrameSize;

    public ThriftDecoder(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        int readableBytes = in.readableBytes();
        if (readableBytes <= 0) {
            return;
        }

        short magicNumber = in.getUnsignedByte(0);
        if (magicNumber >= 0x80) {
            ctx.fireExceptionCaught(new OperationNotSupportedException(
                    "We do not support unframed message at the moment."));
        } else if (readableBytes < MESSAGE_FRAME_SIZE) {
            // we still do not have enough content to read the size of the framed message
            return;
        } else {
            ThriftMessage messageBuffer = decodeFramedMessage(ctx, in);
            if (messageBuffer == null) {
                return;
            }
            out.add(messageBuffer);
        }
    }

    private ThriftMessage decodeFramedMessage(ChannelHandlerContext ctx, ByteBuf in) {

        int messageStartIndex = in.readerIndex();
        int messageContentsOffset = messageStartIndex + MESSAGE_FRAME_SIZE;

        // At this point we know that there's at least 4 bytes available to read an integer
        int messageContentsLength = in.getInt(messageStartIndex);
        int messageLength = messageContentsLength + MESSAGE_FRAME_SIZE;

        if (messageContentsLength > maxFrameSize) {
            ctx.fireExceptionCaught(new TooLongFrameException("Maximum frame size of " + maxFrameSize + " exceeded"));
        }

        if (in.readableBytes() < messageLength) {
            // Full message isn't available yet, return nothing for now
            return null;
        } else {
            // Full message is available, return it
            /*
             * TODO we could increase the reference counter to prevent this messageBuffer from being garbage collected.
             * For the moment we simply allocate a new bytebuf.
             */
            ByteBuf messageBuffer = in.copy(messageContentsOffset, messageContentsLength);
            in.readerIndex(messageStartIndex + messageLength);
            return new ThriftMessage(ThriftTransportType.FRAMED, messageBuffer);
        }
    }

}
