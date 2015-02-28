package io.herd.http;


@FunctionalInterface
public interface Handler {

    void handle(HttpContext context);
}
