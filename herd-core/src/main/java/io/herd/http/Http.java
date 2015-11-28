package io.herd.http;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import io.herd.ServerRuntime;
import io.herd.base.Builder;
import io.herd.base.routing.Route;
import io.herd.base.routing.Routes;
import io.herd.netty.NettyServerRuntime;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.concurrent.ImmediateEventExecutor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

public class Http implements Builder<ServerRuntime> {
    
    private static final Logger logger = getLogger(lookup().lookupClass());
    
    public static final void logRoute(Route<?, ?> route) {
        logger.info("{}", route);
    }

    private HttpConfiguration configuration;
    private int port = -1;
    private List<Route<String, ? extends ChannelHandler>> routes;

    public Http(HttpConfiguration configuration) {
        this.configuration = configuration;
        this.routes = new ArrayList<>();
    }

    @Override
    public ServerRuntime build() {
        this.routes.stream().forEach(Http::logRoute);
        NettyServerRuntime serverRuntime = new NettyServerRuntime("Http", getHandler());
        if (port > 0) {
            serverRuntime.setPort(port);
        } else {
            serverRuntime.setPort(configuration.getPort());
        }
        return serverRuntime;
    }

    public Http get(String path, Handler handler) {
        this.routes.add(Routes.forString(path, new HttpHandler(handler)));
        return this;
    }

    public Http get(String path, String resource) {
        this.routes.add(Routes.forString(path, new HttpStaticResourceHandler(resource)));
        return this;
    }

    public ChannelHandler getHandler() {
        return new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast("http Codec", new HttpServerCodec());
                // adding this allows me to deal with FullHttpRequest on the next handler
                pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                pipeline.addLast("http Handler", new DispatchingHttpHandler(routes));
                pipeline.addLast("websocket handler", new WebSocketServerProtocolHandler("/socket.io"));
                pipeline.addLast("text frame handler", new TextWebSocketFrameHandler(new DefaultChannelGroup(
                        ImmediateEventExecutor.INSTANCE)));
                pipeline.addLast("bad request handler", new BadHTTPRequestHandler());
            }

        };
    }

    public Http listen(int port) {
        this.port = port;
        return this;
    }

}
