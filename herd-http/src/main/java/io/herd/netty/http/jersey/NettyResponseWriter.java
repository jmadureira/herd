package io.herd.netty.http.jersey;

import io.herd.netty.http.HttpResponses;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;

/**
 * Bridge between Netty and Jersey's {@link ContainerResponseWriter}.
 * 
 * @author joaomadureira
 *
 */
class NettyResponseWriter implements ContainerResponseWriter {

    private final HttpVersion protocolVersion;
    private final boolean keepAlive;
    private final ChannelHandlerContext ctx;
    private FullHttpResponse httpResponse;

    public NettyResponseWriter(HttpVersion protocolVersion, boolean keepAlive, ChannelHandlerContext ctx) {
        this.protocolVersion = protocolVersion;
        this.keepAlive = keepAlive;
        this.ctx = ctx;
    }

    private HttpResponseStatus getResponseStatus(ContainerResponse responseContext) {
        return HttpResponseStatus.valueOf(responseContext.getStatus());
    }

    @Override
    public OutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse responseContext)
            throws ContainerException {

        HttpResponseStatus responseStatus = getResponseStatus(responseContext);
        if (HttpResponseStatus.NOT_FOUND == responseStatus) {
            httpResponse = HttpResponses.create404Response(protocolVersion);
        } else {
            httpResponse = new DefaultFullHttpResponse(protocolVersion, responseStatus);
            if (keepAlive) {
                httpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }
        }
        return new ByteBufOutputStream(httpResponse.content());
    }

    @Override
    public boolean suspend(long timeOut, TimeUnit timeUnit, TimeoutHandler timeoutHandler) {
        throw new UnsupportedOperationException("suspend is not supported at the moment");
    }

    @Override
    public void setSuspendTimeout(long timeOut, TimeUnit timeUnit) throws IllegalStateException {
        throw new UnsupportedOperationException("setSuspendTimeout is not supported at the moment");
    }

    @Override
    public void commit() {
        ChannelFuture channelFuture = ctx.write(httpResponse);
        if (!keepAlive) {
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void failure(Throwable error) {
        // TODO instead of firing an exception down the pipeline we could return a 503 error
        ctx.fireExceptionCaught(error);
    }

    @Override
    public boolean enableResponseBuffering() {
        return false;
    }
}
