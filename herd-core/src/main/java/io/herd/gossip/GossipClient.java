package io.herd.gossip;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GossipClient {

    private static final Logger logger = LoggerFactory.getLogger(GossipClient.class);
    
    private final Gossiper gossiper;
    
    public GossipClient(Gossiper gossiper) {
        this.gossiper = gossiper;
    }

    public void gossip(GossipDigestSyn message, InetSocketAddress target) {

        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Channel channel = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new GossipClientInitializer(gossiper))
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .connect(target)
                    .sync()
                    .channel();

            channel.writeAndFlush(message);

            channel.closeFuture().sync();
        } catch (Exception e) {
            logger.error("Failed to gossip with " + target, e);
        } finally {
            group.shutdownGracefully();
        }

    }

}
