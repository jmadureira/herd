package io.herd.netty.http;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.herd.netty.Streams;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.io.InputStream;

/**
 * Static collection of helper methods to create pre-defined {@link FullHttpResponse} messages.
 * 
 * @author joaomadureira
 *
 */
public final class HttpResponses {

    private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-8";

    private static final String PAGE_404 = "/errorpages/404.html";

    private HttpResponses() {

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
            FullHttpResponse response = new DefaultFullHttpResponse(protocolVersion, HttpResponseStatus.NOT_FOUND,
                    buffer404);

            response.headers().add(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
            response.headers().add(CONTENT_LENGTH, response.content().readableBytes());
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
