package io.herd.netty.codec.thrift;

import static org.junit.Assert.assertEquals;
import io.herd.thrift.test.fibonacci;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ThriftChannelTest {

    private static EventLoopGroup group;

    private ThriftChannelInitializer thriftChannelInitializer;

    private ServerBootstrap sb;
    private Channel sc;

    @BeforeClass
    public static void createGroup() {
        group = new NioEventLoopGroup();
    }

    @AfterClass
    public static void destroyGroup() throws Exception {
        group.shutdownGracefully().sync();
    }

    @Before
    public void setUp() throws Exception {
        this.thriftChannelInitializer = new ThriftChannelInitializer(new TProcessorFactory(
                new fibonacci.Processor<fibonacci.Iface>(new FibonacciService())));

        this.sb = new ServerBootstrap().localAddress(new InetSocketAddress(8080)).group(group)
                .channel(NioServerSocketChannel.class).childHandler(this.thriftChannelInitializer);
        this.sc = this.sb.bind().sync().channel();
    }

    @After
    public void tearDown() throws Exception {
        sc.close().sync();
    }

    @Test
    public void testUnframedBinaryTransport() throws Exception {

        TTransport transport = new TSocket("127.0.0.1", 8080);
        transport.open();

        TProtocol protocol = new TBinaryProtocol(transport);

        testCall(protocol);
    }
    
//    @Test
//    public void testUnframedJSONTransport() throws Exception {
//        
//        TTransport transport = new TSocket("127.0.0.1", 8080);
//        transport.open();
//        
//        TProtocol protocol = new TJSONProtocol(transport);
//        
//        testCall(protocol);
//    }

    @Test
    public void testFramedBinaryTransport() throws Exception {

        TTransport transport = new TFramedTransport(new TSocket("127.0.0.1", 8080));
        transport.open();

        TProtocol protocol = new TBinaryProtocol(transport);

        testCall(protocol);
    }

    @Test
    public void testFramedJSONTransport() throws Exception {

        TTransport transport = new TFramedTransport(new TSocket("127.0.0.1", 8080));
        transport.open();

        TProtocol protocol = new TJSONProtocol(transport);

        testCall(protocol);
    }

    @Test
    public void testFramedCompactTransport() throws Exception {

        TTransport transport = new TFramedTransport(new TSocket("127.0.0.1", 8080));
        transport.open();

        TProtocol protocol = new TCompactProtocol(transport);

        testCall(protocol);
    }
    
    private void testCall(TProtocol protocol) throws Exception {

        fibonacci.Client client = new fibonacci.Client(protocol);

        assertEquals(1, client.getFibonacci(0));
        assertEquals(1, client.getFibonacci(1));
        assertEquals(2, client.getFibonacci(2));
        assertEquals(3, client.getFibonacci(3));
        assertEquals(5, client.getFibonacci(4));
        assertEquals(8, client.getFibonacci(5));
        assertEquals(13, client.getFibonacci(6));
        assertEquals(21, client.getFibonacci(7));
        assertEquals(34, client.getFibonacci(8));
        assertEquals(55, client.getFibonacci(9));
        assertEquals(89, client.getFibonacci(10));
    }
}
