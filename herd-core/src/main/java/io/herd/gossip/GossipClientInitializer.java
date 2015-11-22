package io.herd.gossip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

class GossipClientInitializer extends ChannelInitializer<SocketChannel> {
    
    private static final Logger logger = LoggerFactory.getLogger(GossipClientInitializer.class);
    
    private final Gossiper gossiper;
    
    GossipClientInitializer(Gossiper gossiper) {
        this.gossiper = gossiper;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        
        logger.debug("Initializing gossip client channel");
        
        ChannelPipeline pipeline = ch.pipeline();
        
        pipeline.addLast("synDecoder", new GossipMessageDecoder());
        pipeline.addLast("synEncoder", new GossipDigestSynEncoder());
        pipeline.addLast("ackEncoder", new GossipDigestAckEncoder());
        pipeline.addLast("ack2Encoder", new GossipDigestAck2Encoder());
        
        pipeline.addLast("synHandler", new GossipDigestSynHandler(gossiper));
        pipeline.addLast("ackHandler", new GossipDigestAckHandler(gossiper));
    }

}
