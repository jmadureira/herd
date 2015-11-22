package io.herd.gossip;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class GossipMessageDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(GossipMessageDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (in.readableBytes() < 5) {
            return;
        }

        in.markReaderIndex();

        Object message = null;
        // Check the magic number.
        char magicNumber = (char) in.readUnsignedByte();
        switch (magicNumber) {
        case 'S':
            message = GossipDigestSyn.serializer.deserialize(ctx, in);
            break;
        case 'A':
            message = GossipDigestAck.serializer.deserialize(ctx, in);
            break;
        case 'E':
            message = GossipDigestAck2.serializer.deserialize(ctx, in);
            break;
        default:
            logger.debug("Unexpected magic number '{}'.", magicNumber);
            break;
        }

        if(message == null) {
            logger.debug("Message is not a gossip message.");
            in.resetReaderIndex();
            return;
        }
        out.add(message);
    }

}
