package io.herd.http;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Objects;

@JsonTypeName("default")
public class DefaultHttpConfiguration implements HttpConfiguration {

    private int port = DEFAULT_HTTP_PORT;

    /**
     * Defaults to {@link HttpConfiguration#DEFAULT_HTTP_PORT}.
     */
    @Override
    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    @Min(1)
    @Max(Short.MAX_VALUE)
    public void setPort(int port) {
        this.port = port;
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("port", port)
                .toString();
    }

}
