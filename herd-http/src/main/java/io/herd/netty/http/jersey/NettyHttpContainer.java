package io.herd.netty.http.jersey;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;

public class NettyHttpContainer extends SimpleChannelInboundHandler<DefaultHttpRequest> implements Container {

    private final ApplicationHandler applicationHandler;

    public NettyHttpContainer(Application application) {
        this.applicationHandler = new ApplicationHandler(application);
    }

    @Override
    public ResourceConfig getConfiguration() {
        return applicationHandler.getConfiguration();
    }

    @Override
    public ApplicationHandler getApplicationHandler() {
        return applicationHandler;
    }

    @Override
    public void reload() {
        throw new UnsupportedOperationException("reload is not supported at the moment");
    }

    @Override
    public void reload(ResourceConfig configuration) {
        throw new UnsupportedOperationException("reload is not supported at the moment");
    }

    private final void send100Continue(ChannelHandlerContext ctx) {
        ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, DefaultHttpRequest msg) throws Exception {

        if (HttpHeaders.is100ContinueExpected(msg)) {
            send100Continue(ctx);
        }

        String scheme = null;
        String userName = null;
        String password = null;
        boolean isSecure = false;

        SecurityContext securityContext = SecurityContextFactory.create(userName, password, isSecure, scheme);

        URI baseUri = getBaseUri(msg);
        URI requestURI = new URI(msg.getUri());
        ContainerRequest containerRequest = new ContainerRequest(baseUri, requestURI, msg.getMethod().toString(),
                securityContext, new MapPropertiesDelegate());
        
        boolean keepAlive = HttpHeaders.isKeepAlive(msg);

        containerRequest.setWriter(new NettyResponseWriter(msg.getProtocolVersion(), keepAlive, ctx));

        applicationHandler.handle(containerRequest);
    }

    private URI getBaseUri(final DefaultHttpRequest msg) {
        try {
            String host = HttpHeaders.getHost(msg);
            //TODO hard-coding the scheme and the context path might not be best way to do this :p
            return new URI("http", host, "/", null, null);
        } catch (final URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

}
