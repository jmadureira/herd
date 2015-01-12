package io.herd.gossip;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GossipDigestSynHandler extends SimpleChannelInboundHandler<GossipDigestSyn> {

    private static final Logger logger = LoggerFactory.getLogger(GossipDigestSynHandler.class);
    
    private final Gossiper gossiper;
    
    public GossipDigestSynHandler(Gossiper gossiper) {
        this.gossiper = gossiper;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Unexpected error caught when handling request.", cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GossipDigestSyn msg) throws Exception {
        logger.debug("Received " + msg);
        
        List<GossipDigest> digests = msg.getDigest();
        
        List<GossipDigest> deltaDigests = new ArrayList<>();
        Map<InetSocketAddress, EndpointState> nodeStateMap = new HashMap<>();
        
        
        gossiper.determineEndpointStateDeltas(digests, deltaDigests, nodeStateMap);
        ctx.write(new GossipDigestAck(deltaDigests, nodeStateMap));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.debug("GossipDigestSynHandler finished reading channel");
        ctx.flush();
    }
    
    
}
