package io.herd.thrift;

import io.herd.netty.codec.thrift.ThriftMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

public class ThriftHandler extends SimpleChannelInboundHandler<ThriftMessage> {

    private final TProcessorFactory processorFactory;

    public ThriftHandler(TProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, final ThriftMessage message) throws Exception {
        try {

            Factory factory = new TBinaryProtocol.Factory(true, true);

            TTransport trans = TTransports.getTransport(message.content());
            
            TProtocol protocol = factory.getProtocol(trans);

            processorFactory.getProcessor(trans).process(protocol, protocol);
            
            ctx.write(trans);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

}
