package io.herd.http;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.cookie.Cookie;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    
    private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-8";

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Handler handler;

    public HttpHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {

        boolean keepAlive = isKeepAlive(request);
        String uri = request.getUri();
        final Set<Cookie> cookies = getCookies(request);
        final DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        response.headers().add(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);

        handler.handle(new HttpContext() {

            @Override
            public HttpContext setContent(String content) {
                response.content().writeBytes(content.getBytes(Charset.forName("UTF-8")));
                return this;
            }

            @Override
            public Cookie getCookie(final String cookieName) {
                return cookies.stream().filter(new Predicate<Cookie>() {

                    @Override
                    public boolean test(Cookie cookie) {
                        return cookieName.equals(cookie.name());
                    }
                }).findFirst().orElse(null);
            }

            @Override
            public String getHeader(String headerName) {
                return request.headers().get(headerName);
            }

            @Override
            public HttpContext setHeader(String headerName, Object value) {
                response.headers().set(headerName, value);
                return this;
            }

            @Override
            public HttpContext addHeader(String headerName, Object value) {
                response.headers().add(headerName, value);
                return this;
            }

        });
        response.headers().add(CONTENT_LENGTH, response.content().readableBytes());
        if (!keepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, Values.KEEP_ALIVE);
            ctx.write(response);
        }
    }
    
    private Set<Cookie> getCookies(FullHttpRequest req) {
        String cookies = req.headers().get(COOKIE);
        if (cookies != null) {
            // TODO decode the cookies ... later :P
        }
        return new HashSet<Cookie>();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        super.channelReadComplete(ctx);
    }

}
