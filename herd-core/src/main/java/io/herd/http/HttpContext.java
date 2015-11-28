package io.herd.http;

import io.netty.handler.codec.http.cookie.Cookie;

public interface HttpContext {

    HttpContext setContent(String content);

    Cookie getCookie(String cookieName);

    String getHeader(String headerName);

    HttpContext setHeader(String headerName, Object value);

    HttpContext addHeader(String headerName, Object value);
}
