package io.herd.gossip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.herd.ServerRuntime;
import io.herd.base.Interwebs;
import io.herd.base.ServerRuntimeException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

class GossipServer implements ServerRuntime {

    private static final Logger logger = LoggerFactory.getLogger(GossipServer.class);

    private final String serverName;
    private final int port;
    private final Gossiper gossiper;

    // in case we need this to do something.
    private ServerBootstrap serverBootstrap;

    private ChannelFuture channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    
    private volatile boolean isRunning = false;

    public GossipServer(String serverName, int port, Gossiper gossiper) {
        this.serverName = serverName;
        this.port = port == 0 ? Interwebs.findFreePort() : port;
        this.gossiper = gossiper;
    }

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.serverBootstrap = new ServerBootstrap();
        this.serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new GossipServerInitializer(gossiper))
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        try {
            this.channel = this.serverBootstrap.bind(port).sync();
            gossiper.start();
        } catch (Exception e) {
            throw new ServerRuntimeException(e);
        }
        logger.info("Server {} started on port {}", serverName, port);

        isRunning = true;
    }

    @Override
    public void stop() {
        try {
            logger.info("Shutting down server {}...", serverName);
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            this.channel.channel().closeFuture().sync();
            gossiper.stop();
            isRunning = false;
        } catch (Exception e) {
            logger.error("Failed to gracefully shutdown server {} due to {}", serverName, e.toString());
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
