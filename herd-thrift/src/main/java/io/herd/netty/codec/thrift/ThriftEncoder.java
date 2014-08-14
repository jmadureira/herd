package io.herd.thrift;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.TooLongFrameException;

/**
 * 
 * @author joaomadureira
 */
public class ThriftFrameEncoder extends MessageToByteEncoder<TByteBufTransport> {

    private static final String MESSAGE_FRAME_TOO_LONG = "Frame with size %d has exceeded maximum allowed size of %d bytes.";

    private final int maxFrameSize;

    public ThriftFrameEncoder(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, TByteBufTransport msg, ByteBuf out) throws Exception {

        ByteBuf dataBuffer = msg.getOutputBuffer();
        int frameSize = dataBuffer.readableBytes();

        if (frameSize > maxFrameSize) {
            ctx.fireExceptionCaught(new TooLongFrameException(String.format(MESSAGE_FRAME_TOO_LONG, frameSize,
                    maxFrameSize)));
        } else {
            // TODO Add support for the remaining transport types
            out.writeInt(frameSize);
            out.writeBytes(dataBuffer);
        }
    }

}
