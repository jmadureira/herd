package io.herd.gossip;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

class GossipServerInitializer extends ChannelInitializer<Channel> {

    private final Gossiper gossiper;

    public GossipServerInitializer(Gossiper gossiper) {
        this.gossiper = gossiper;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("synDecoder", new GossipMessageDecoder());
        pipeline.addLast("synEncoder", new GossipDigestSynEncoder());
        pipeline.addLast("ackEncoder", new GossipDigestAckEncoder());

        pipeline.addLast("handler", new GossipDigestSynHandler(gossiper));
        pipeline.addLast("ackHandler", new GossipDigestAckHandler(gossiper));
        pipeline.addLast("ack2Handler", new GossipDigestAck2Handler(gossiper));
    }

}
