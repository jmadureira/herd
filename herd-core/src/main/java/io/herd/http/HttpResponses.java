package io.herd.http;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.herd.base.Streams;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Static collection of helper methods to create pre-defined {@link FullHttpResponse} messages.
 * 
 * @author joaomadureira
 *
 */
public final class HttpResponses {

    private static final String HTML_CONTENT_TYPE = "text/html; charset=UTF-8";
    private static final String TEXT_CONTENT_TYPE = "text/plain; charset=UTF-8";

    private static final String PAGE_404 = "/errorpages/404.html";

    private HttpResponses() {

    }
    
    public static final FullHttpResponse createErrorResponse(HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status, CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, TEXT_CONTENT_TYPE);
        
        return response;
    }

    /**
     * 
     * @return A standard 404 response for {@link HttpVersion#HTTP_1_1} protocol.
     * @see #create404Response(HttpVersion)
     */
    public static final FullHttpResponse create404Response() {
        return create404Response(HTTP_1_1);
    }

    public static final FullHttpResponse create404Response(HttpVersion protocolVersion) {
        try (InputStream resource = HttpResponses.class.getResourceAsStream(PAGE_404)) {
            ByteBuf buffer404 = Streams.readToByteBuf(resource);
            FullHttpResponse response = new DefaultFullHttpResponse(protocolVersion, NOT_FOUND, buffer404);

            response.headers().add(CONTENT_TYPE, HTML_CONTENT_TYPE);
            HttpHeaders.setContentLength(response, response.content().readableBytes());
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
