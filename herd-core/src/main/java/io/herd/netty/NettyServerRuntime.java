package io.herd.netty;

import java.lang.invoke.MethodHandles;

import io.herd.ServerRuntime;
import io.herd.base.ServerRuntimeException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServerRuntime implements ServerRuntime {
    
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // in case we need this to do something.
    private ServerBootstrap serverBootstrap;

    private ChannelFuture channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    
    private int port;
    
    private volatile boolean isRunning = false;
    
    private final String serverName;
    private final ChannelHandler handler;

    
    public NettyServerRuntime(String serverName, ChannelHandler handler) {
        this.serverName = serverName;
        this.handler = handler;
    }
    
    public int getPort() {
        return port;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public void start() {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(handler)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        try {
            this.channel = this.serverBootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            throw new ServerRuntimeException(e);
        }
        
        logger.info("Server {} started on server {}", serverName, port);
        isRunning = true;
    }

    public void stop() {
        try {
            logger.info("Shutting down server {}...", serverName);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            this.channel.channel().closeFuture().sync();
            isRunning = false;
        } catch (Exception e) {
            logger.error("Failed to gracefully shutdown server {} due to {}", serverName, e.toString());
        }
    }

}
