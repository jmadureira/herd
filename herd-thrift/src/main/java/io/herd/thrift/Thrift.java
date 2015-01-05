package io.herd.thrift;

import io.herd.ServerRuntime;
import io.herd.base.Builder;
import io.herd.netty.NettyServerRuntime;
import io.herd.netty.codec.thrift.ThriftChannelInitializer;
import io.netty.channel.ChannelHandler;

import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;

public class Thrift implements Builder<ServerRuntime> {

    private static final String DEFAULT_NAME = "Thrift";

    private TProcessorFactory tProcessorFactory;
    private ThriftConfiguration configuration;

    private int port = -1;
    private String serviceName;

    public Thrift() {
        this(DEFAULT_NAME, new DefaultThriftConfiguration());
    }

    /**
     * Creates a new builder for thrift services using the provided configuration.
     * 
     * @param configuration The configuration that will be used. If
     *            <code>null<code> is passed it will default to the thrift default configurations.
     * @see #Thrift(String, ThriftConfiguration)
     */
    public Thrift(ThriftConfiguration configuration) {
        this(DEFAULT_NAME, configuration);
    }

    /**
     * 
     * @param serviceName The name of service being built. Can be changed afterwards and before calling {@link #build()}
     *            by using {@link #named(String)}.
     * @param configuration The configuration that will be used. If
     *            <code>null<code> is passed it will default to the thrift default configurations.
     */
    public Thrift(String serviceName, ThriftConfiguration configuration) {
        this.serviceName = serviceName;
        this.configuration = configuration == null ? new DefaultThriftConfiguration() : configuration;
    }

    public Thrift named(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    @Override
    public ServerRuntime build() {
        NettyServerRuntime serverRuntime = new NettyServerRuntime(serviceName, getHandler());
        if (port > 0) {
            serverRuntime.setPort(port);
        } else {
            serverRuntime.setPort(configuration.getPort());
        }
        return serverRuntime;
    }

    public Thrift listen(int port) {
        this.port = port;
        return this;
    }

    private ChannelHandler getHandler() {
        return new ThriftChannelInitializer(tProcessorFactory);
    }

    public Thrift serving(TProcessor resource) {
        tProcessorFactory = new TProcessorFactory(resource);
        return this;
    }

}
