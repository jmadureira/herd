package io.herd.thrift;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Objects;

@JsonTypeName("default")
public class DefaultThriftConfiguration implements ThriftConfiguration {

    private int port = 9090;

    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("port", port)
                .toString();
    }

}
