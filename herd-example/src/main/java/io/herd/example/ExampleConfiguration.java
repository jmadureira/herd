package io.herd.example;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.herd.base.Configuration;
import io.herd.http.DefaultHttpConfiguration;
import io.herd.http.HttpConfiguration;
import io.herd.thrift.DefaultThriftConfiguration;
import io.herd.thrift.ThriftConfiguration;

public class ExampleConfiguration extends Configuration {

    @Valid
    @NotNull
    private HttpConfiguration http = new DefaultHttpConfiguration();

    @Valid
    @NotNull
    private ThriftConfiguration thrift = new DefaultThriftConfiguration();

    /**
     * Returns the server-specific section of the configuration file.
     *
     * @return server-specific configuration parameters
     */
    @JsonProperty("http")
    public HttpConfiguration getServerFactory() {
        return http;
    }

    /**
     * Sets the HTTP-specific section of the configuration file.
     */
    @JsonProperty("http")
    public void setServerFactory(HttpConfiguration factory) {
        this.http = factory;
    }

    /**
     * Returns the server-specific section of the configuration file.
     *
     * @return server-specific configuration parameters
     */
    @JsonProperty("thrift")
    public ThriftConfiguration getThriftFactory() {
        return thrift;
    }

    /**
     * Sets the Thrift-specific section of the configuration file.
     */
    @JsonProperty("thrift")
    public void setThriftFactory(ThriftConfiguration factory) {
        this.thrift = factory;
    }

}
