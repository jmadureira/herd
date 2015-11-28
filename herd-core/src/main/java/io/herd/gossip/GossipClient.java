package io.herd.gossip;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GossipClient {

    private static final Logger logger = LoggerFactory.getLogger(GossipClient.class);
    
    private NioEventLoopGroup group;
    private Bootstrap bootstrap;
    
    public GossipClient(Gossiper gossiper) {
        this.group = new NioEventLoopGroup(1);
        this.bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new GossipClientInitializer(gossiper))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
    }

    public void gossip(GossipDigestSyn message, InetSocketAddress target) {
        try {
            Channel channel = bootstrap
                    .connect(target)
                    .sync()
                    .channel();

            channel.writeAndFlush(message);

            channel.closeFuture().sync();
        } catch (Exception e) {
            logger.error("Failed to gossip with " + target, e);
        }

    }

    public void stop() {
        this.group.shutdownGracefully();
    }

}
