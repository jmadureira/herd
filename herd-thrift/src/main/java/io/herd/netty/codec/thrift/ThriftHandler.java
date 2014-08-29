package io.herd.netty.codec.thrift;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ThriftHandler extends SimpleChannelInboundHandler<ThriftMessage> {
    
    private static final Logger logger = LoggerFactory.getLogger(ThriftHandler.class);

    private final TProcessorFactory processorFactory;

    public ThriftHandler(TProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, final ThriftMessage message) throws Exception {
        try {

            TProtocolFactory factory = ThriftProtocolUtil.guessProtocolFactory(message);

            TTransport out = new ThriftMessage(message.getTransportType());
            
            TProtocol inProtocol = factory.getProtocol(message);
            TProtocol outProtocol = factory.getProtocol(out);

            processorFactory.getProcessor(message).process(inProtocol, outProtocol);
            
            ctx.write(out);
        } catch (Exception e) {
            logger.error("Failed to read thrift message due to {}", e.toString());
            e.printStackTrace();
        }
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

}
