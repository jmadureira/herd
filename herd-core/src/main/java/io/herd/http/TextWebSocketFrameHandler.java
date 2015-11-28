package io.herd.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    
    private final ChannelGroup group;

    public TextWebSocketFrameHandler(ChannelGroup group) {
        this.group = group;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {

            ctx.pipeline().remove(BadHTTPRequestHandler.class);
            ctx.pipeline().remove(DispatchingHttpHandler.class);

            group.writeAndFlush(new TextWebSocketFrame("Client " + ctx.channel() + " joined"));

            group.add(ctx.channel());
            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() ->{
                group.writeAndFlush(new TextWebSocketFrame("ping"));
            }, 1000, 1000, TimeUnit.MILLISECONDS);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        group.writeAndFlush(msg.retain());
    }

}
