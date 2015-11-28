package io.herd.http;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import io.herd.base.routing.Route;
import io.herd.base.routing.Route.Routed;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;

class DispatchingHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = getLogger(lookup().lookupClass());

    private final List<Route<String, ? extends ChannelHandler>> routes;

    public DispatchingHttpHandler(List<Route<String, ? extends ChannelHandler>> routes) {
        this.routes = routes;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, final FullHttpRequest req) {

        logger.info("Received request from {}", req.getUri());
        // This isn't necessary if we're using a HttpObjectAggregator
        // if (is100ContinueExpected(req)) {
        // ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        // }
        QueryStringDecoder decoder = new QueryStringDecoder(req.getUri());
        String path = decoder.path();
        
        if ("/favicon.ico".equals(req.getUri())) {
            FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
            if (res.getStatus().code() != 200) {
                ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
                res.content().writeBytes(buf);
                buf.release();
                HttpHeaders.setContentLength(res, res.content().readableBytes());
            }

            // Send the response and close the connection if necessary.
            ChannelFuture f = ctx.channel().writeAndFlush(res);
            if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
            return;
        }
        
        for (Route<String, ? extends ChannelHandler> route : routes) {
            Routed<? extends ChannelHandler> target = route.route(path);
            if (target.match()) {
                ChannelHandler newHandler = target.getTarget();
                ChannelHandler handler = ctx.pipeline().get(route.getPattern());
                if (handler == null) {
                    ctx.pipeline().addAfter("http Handler", route.getPattern(), newHandler);
                }
                for (Entry<String, String> attr : target.getAttributes().entrySet()) {
                    req.headers().add(attr.getKey(), attr.getValue());
                }
                break;
            }
        }
        req.retain();
        ctx.fireChannelRead(req);
    }

}
