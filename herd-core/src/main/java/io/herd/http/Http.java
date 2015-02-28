package io.herd.http;

import io.herd.ServerRuntime;
import io.herd.base.Builder;
import io.herd.netty.NettyServerRuntime;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Http implements Builder<ServerRuntime> {

    private HttpConfiguration configuration;
    private int port = -1;
    private Map<String, Handler> resources;
    private List<String> staticResources;

    public Http(HttpConfiguration configuration) {
        this.configuration = configuration;
        this.staticResources = new ArrayList<>();
        this.resources = new HashMap<>();
    }

    @Override
    public ServerRuntime build() {
        NettyServerRuntime serverRuntime = new NettyServerRuntime("Http", getHandler());
        if (port > 0) {
            serverRuntime.setPort(port);
        } else {
            serverRuntime.setPort(configuration.getPort());
        }
        return serverRuntime;
    }
    
    public Http listen(int port) {
        this.port = port;
        return this;
    }

    public ChannelHandler getHandler() {
        List<Route> routes = resources
              .entrySet()
              .stream()
              .map((entry) -> new Route(entry.getKey(), entry.getValue()))
              .collect(Collectors.toList());
        return new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast("http Codec", new HttpServerCodec());
                // adding this allows me to deal with FullHttpRequest on the next handler
                pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                pipeline.addLast("http Handler", new DispatchingHttpHandler(routes));
//                pipeline.addLast("websocket handler", new WebSocketServerProtocolHandler("/socket.io"));
//                pipeline.addLast("text frame handler", new TextWebSocketFrameHandler(new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE)));
                pipeline.addLast("bad request handler", new BadHTTPRequestHandler());
            }

        };
    }

    public Http serving(String contextPath, Handler handler) {
        this.resources.put(contextPath, handler);
        return this;
    }

    public Http servingResources(String path) {
        this.staticResources.add(path);
        return this;
    }

}
