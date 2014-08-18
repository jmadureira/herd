package io.herd.netty.codec.thrift;

import org.apache.thrift.transport.TTransportException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.TooLongFrameException;

/**
 * 
 * @author joaomadureira
 */
class ThriftEncoder extends MessageToByteEncoder<ThriftMessage> {

    private static final String MESSAGE_FRAME_TOO_LONG = "Frame with size %d has exceeded maximum allowed size of %d bytes.";

    private final int maxFrameSize;

    public ThriftEncoder(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ThriftMessage msg, ByteBuf out) throws Exception {

        ByteBuf dataBuffer = msg.content();
        int frameSize = dataBuffer.readableBytes();

        if (frameSize > maxFrameSize) {
            ctx.fireExceptionCaught(new TooLongFrameException(String.format(MESSAGE_FRAME_TOO_LONG, frameSize,
                    maxFrameSize)));
        } else {
            switch (msg.getTransportType()) {
            case FRAMED:
                out.writeInt(frameSize);
                out.writeBytes(dataBuffer);
                break;
            case UNFRAMED:
                out.writeBytes(dataBuffer);
                break;
            // TODO Add support for the remaining transport types
            default:
                ctx.fireExceptionCaught(new TTransportException("Unsupported transport type " + msg.getTransportType()));
            }
        }
    }

}
