package io.herd.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import io.herd.base.Buffers;
import io.herd.base.routing.Route;
import io.herd.base.routing.Routes;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.collect.Lists;

@RunWith(JUnit4.class)
public class DispatchingHttpHandlerTest {

    private static EventLoopGroup group;

    private EmbeddedChannel sc;

    @BeforeClass
    public static void createGroup() {
        group = new NioEventLoopGroup();
    }

    @AfterClass
    public static void destroyGroup() throws Exception {
        group.shutdownGracefully().sync();
    }
    
    @Test
    public void testSetHeaders() throws Exception {
//        Handler handler = (ctx) -> {
//            ctx.setHeader("someHeader", "some value");
//        };
//        List<Route<String, ? extends ChannelHandler>> handlers = new ArrayList<>();
//        handlers.add(Routes.forString("/", new HttpHandler(handler)));
//        this.sc = new EmbeddedChannel(new DispatchingHttpHandler(handlers));
//
//        this.sc.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"));
//        assertTrue(this.sc.finish());
//
//        DefaultFullHttpResponse response = (DefaultFullHttpResponse) this.sc.readOutbound();
//        assertEquals("some value", response.headers().get("someHeader"));
    }
    
//    @Test
//    public void testAddHeaders() throws Exception {
//        Handler handler = (ctx) -> {
//            ctx.addHeader("someHeader", "value1");
//            ctx.addHeader("someHeader", "value2");
//        };
//        List<Route<String, ? extends ChannelHandler>> handlers = new ArrayList<>();
//        handlers.add(Routes.forString("/", new HttpHandler(handler)));
//        this.sc = new EmbeddedChannel(new DispatchingHttpHandler(handlers));
//        
//        this.sc.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"));
//        assertTrue(this.sc.finish());
//        
//        DefaultFullHttpResponse response = (DefaultFullHttpResponse) this.sc.readOutbound();
//        assertEquals(Lists.newArrayList("value1", "value2"), response.headers().getAll("someHeader"));
//    }
    
//    @Test
//    public void testSetContent() throws Exception {
//        Handler handler = (ctx) -> {
//            ctx.setContent("Hello");
//        };
//        List<Route<String, ? extends ChannelHandler>> handlers = new ArrayList<>();
//        handlers.add(Routes.forString("/", new HttpHandler(handler)));
//        this.sc = new EmbeddedChannel(new DispatchingHttpHandler(handlers));
//        
//        this.sc.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"));
//        assertTrue(this.sc.finish());
//        
//        DefaultFullHttpResponse response = (DefaultFullHttpResponse) this.sc.readOutbound();
//        assertEquals("Hello", Buffers.readString(response.content()));
//    }
}
