package io.herd.gossip;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

class GossipClientInitializer extends ChannelInitializer<SocketChannel> {
    
    private final Gossiper gossiper;
    
    GossipClientInitializer(Gossiper gossiper) {
        this.gossiper = gossiper;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        
        pipeline.addLast("synDecoder", new GossipMessageDecoder());
        pipeline.addLast("synEncoder", new GossipDigestSynEncoder());
        pipeline.addLast("ackEncoder", new GossipDigestAckEncoder());
        pipeline.addLast("ack2Encoder", new GossipDigestAck2Encoder());
        
        pipeline.addLast("synHandler", new GossipDigestSynHandler(gossiper));
        pipeline.addLast("ackHandler", new GossipDigestAckHandler(gossiper));
    }

}
