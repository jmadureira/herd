package io.herd.http;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultHttpConfiguration.class)
public interface HttpConfiguration {

    /**
     * The system's default HTTP port. Currently {@value #DEFAULT_HTTP_PORT}.
     */
    int DEFAULT_HTTP_PORT = 8080;

    /**
     * 
     * @return The port number for this http server.
     */
    int getPort();
}
