package io.herd.gossip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

class GossipServerInitializer extends ChannelInitializer<Channel> {

    private static final Logger logger = LoggerFactory.getLogger(GossipServerInitializer.class);
    
    private final Gossiper gossiper;

    public GossipServerInitializer(Gossiper gossiper) {
        this.gossiper = gossiper;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {

        logger.debug("Initializing gossip server channel");

        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("synDecoder", new GossipMessageDecoder());
        pipeline.addLast("synEncoder", new GossipDigestSynEncoder());
        pipeline.addLast("ackEncoder", new GossipDigestAckEncoder());

        pipeline.addLast("handler", new GossipDigestSynHandler(gossiper));
        pipeline.addLast("ackHandler", new GossipDigestAckHandler(gossiper));
        pipeline.addLast("ack2Handler", new GossipDigestAck2Handler(gossiper));
    }

}
