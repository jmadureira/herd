package io.herd.netty.codec.thrift;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import org.apache.thrift.TProcessorFactory;

public class ThriftChannelInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * The maximum thrift message size is set to 16MB. The reasoning behind this is that if you're sending messages
     * bigger than this you're doing something wrong.
     */
    private static final int MAX_FRAME_SIZE = 16 * 1024 * 1024;

    private TProcessorFactory processorFactory;

    public ThriftChannelInitializer(TProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("thriftCodec", new ThriftCodec(MAX_FRAME_SIZE));
    }

}
