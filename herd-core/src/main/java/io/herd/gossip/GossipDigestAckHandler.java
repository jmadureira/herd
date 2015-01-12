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

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GossipDigestAck msg) throws Exception {
        logger.debug("Got back {}", msg);
        
        // the list of digests the gossipee asked for
        List<GossipDigest> digestList = msg.getDigest();
        // an updated state of endpoints we should update on our side
        Map<InetSocketAddress, EndpointState> newEndpointStates = msg.getEndpointStates();
        
        if(newEndpointStates.size() > 0) {
            Gossiper.instance.updateStates(newEndpointStates);
        }
        
        Map<InetSocketAddress, EndpointState> nodeStateMap = new HashMap<>();
        for(GossipDigest digest : digestList) {
            InetSocketAddress address = digest.endpoint;
            EndpointState endpointState = Gossiper.instance.getEndpointState(address, digest.maxVersion);
            if(endpointState != null) {
                nodeStateMap.put(address, endpointState);
            }
            
        }
        ctx.writeAndFlush(new GossipDigestAck2(nodeStateMap)).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Unexpected exception from downstream.", cause);
        ctx.close();
    }

}
