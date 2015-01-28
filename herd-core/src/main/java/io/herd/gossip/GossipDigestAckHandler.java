package io.herd.gossip;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GossipDigestAckHandler extends SimpleChannelInboundHandler<GossipDigestAck> {

    private static final Logger logger = LoggerFactory.getLogger(GossipDigestAckHandler.class);

    private final Gossiper gossiper;

    GossipDigestAckHandler(Gossiper gossiper) {
        this.gossiper = gossiper;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GossipDigestAck msg) throws Exception {
        logger.trace("Received {}", msg);

        // the list of digests the gossipee asked for
        List<GossipDigest> digestList = msg.getDigest();
        // update any new state on our side if necessary
        gossiper.updateStates(msg.getEndpointStates());

        // now get the endpoint state requested by the other guy
        Map<InetSocketAddress, EndpointState> nodeStateMap = new HashMap<>();
        for (GossipDigest digest : digestList) {
            this.gossiper.sendDelta(digest, nodeStateMap, digest.maxVersion);
        }
        logger.trace("Sending back new states: {}", nodeStateMap);
        ctx.writeAndFlush(new GossipDigestAck2(nodeStateMap)).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Unexpected exception from downstream.", cause);
        ctx.close();
    }

}
