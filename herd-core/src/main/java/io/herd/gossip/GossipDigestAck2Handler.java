package io.herd.gossip;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GossipDigestAck2Handler extends SimpleChannelInboundHandler<GossipDigestAck2> {

    private static final Logger logger = LoggerFactory.getLogger(GossipDigestAck2Handler.class);

    private final Gossiper gossiper;

    GossipDigestAck2Handler(Gossiper gossiper) {
        this.gossiper = gossiper;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GossipDigestAck2 msg) throws Exception {
        logger.trace("Got back {}", msg);

        // an updated state of endpoints we should update on our side
        gossiper.updateStates(msg.getEndpointStates());
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Unexpected exception from downstream.", cause);
        ctx.close();
    }

}
