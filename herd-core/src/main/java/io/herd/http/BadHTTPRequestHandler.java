package io.herd.http;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.lang.invoke.MethodHandles.lookup;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler that always returns a BAD_REQUEST {@link HttpResponse} so it should be registered at the end of the pipeline
 * to catch all request that weren't caught be any previous handler.
 */
@Sharable
public class BadHTTPRequestHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(lookup().lookupClass());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;

            HttpResponse res = new DefaultHttpResponse(HTTP_1_1, BAD_REQUEST);
            ctx.channel().writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
            req.release();
            logger.debug("Caught wrong http request on {}", req.getUri());
        } else {
            super.channelRead(ctx, msg);
        }
    }

}
