package io.herd.netty.codec.thrift;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolUtil;
import org.apache.thrift.protocol.TType;

class ThriftDecoder extends ByteToMessageDecoder {

    /**
     * {@value #MESSAGE_FRAME_SIZE} bytes to encode an integer.
     */
    public static final int MESSAGE_FRAME_SIZE = 4;

    private final int maxFrameSize;

    public ThriftDecoder(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        ThriftMessage message = null;
        int readableBytes = in.readableBytes();
        if (readableBytes <= 0) {
            return;
        }

        short magicNumber = in.getUnsignedByte(0);
        if (magicNumber >= 0x80) {
            message = decodeUnframedMessage(ctx, in);
        } else if (readableBytes < MESSAGE_FRAME_SIZE) {
            // we still do not have enough content to read the size of the framed message
            return;
        } else {
            message = decodeFramedMessage(ctx, in);
        }
        if (message != null) {
            out.add(message);
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

    private ThriftMessage decodeUnframedMessage(ChannelHandlerContext ctx, ByteBuf in) {

        in.markReaderIndex();
        int messageLength = 0;
        int initialReadBytes = in.readerIndex();

        try {
            // TODO we only support binary protocol at the moment but nevertheless this should not be here.
            Factory factory = new TBinaryProtocol.Factory(true, true);
            ThriftMessage decodedMessage = new ThriftMessage(ThriftTransportType.UNFRAMED, in);
            TProtocol inputProtocol = factory.getProtocol(decodedMessage);

            /*
             * The trick is to perform a decode without actually creating the message. If successfully we can determine
             * the message length and copy the buffer to the thrift message.
             */
            inputProtocol.readMessageBegin();
            TProtocolUtil.skip(inputProtocol, TType.STRUCT);
            inputProtocol.readMessageEnd();

            messageLength = decodedMessage.content().readerIndex() - initialReadBytes;
        } catch (TException | IndexOutOfBoundsException e) {
            // No complete message was decoded: ran out of bytes
            return null;
        } finally {
            if (messageLength > maxFrameSize) {
                ctx.fireExceptionCaught(new TooLongFrameException("Maximum frame size of " + maxFrameSize + " exceeded"));
            }

            in.resetReaderIndex();
        }

        if (messageLength <= 0) {
            return null;
        }

        // We have a full message in the read buffer, slice it off
        ByteBuf messageBuffer = in.copy(initialReadBytes, messageLength);
        in.readerIndex(initialReadBytes + messageLength);
        return new ThriftMessage(ThriftTransportType.UNFRAMED, messageBuffer);
    }

}
