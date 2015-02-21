package io.herd.thrift;

import static io.herd.base.Preconditions.checkNotEmpty;
import static io.herd.base.Preconditions.checkValueRange;
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

    Thrift() {
        this(DEFAULT_NAME, new DefaultThriftConfiguration());
    }

    /**
     * 
     * @param serviceName The name of service being built. Can be changed afterwards and before calling {@link #build()}
     *            by using {@link #named(String)}.
     * @param configuration The configuration that will be used. If
     *            <code>null<code> is passed it will default to the thrift default configurations.
     */
    Thrift(String serviceName, ThriftConfiguration configuration) {
        this.serviceName = serviceName;
        this.configuration = configuration == null ? new DefaultThriftConfiguration() : configuration;
    }

    /**
     * Creates a new builder for thrift services using the provided configuration.
     * 
     * @param configuration The configuration that will be used. If
     *            <code>null<code> is passed it will default to the thrift default configurations.
     * @see #Thrift(String, ThriftConfiguration)
     */
    Thrift(ThriftConfiguration configuration) {
        this(DEFAULT_NAME, configuration);
    }

    @Override
    public ServerRuntime build() {
        NettyServerRuntime serverRuntime = new NettyServerRuntime(serviceName, getHandler());
        serverRuntime.setPort(port > 0 ? port : configuration.getPort());
        
        return serverRuntime;
    }

    private ChannelHandler getHandler() {
        return new ThriftChannelInitializer(tProcessorFactory);
    }

    /**
     * Overrides the current port configuration to the specified value.
     * 
     * @param port The new port where this service will be listening to
     * @return This builder to allow chaining.
     * @throws IllegalArgumentException if the port value is invalid
     */
    public Thrift listen(int port) {
        this.port = checkValueRange(port, 1, Short.MAX_VALUE,
                String.format("Port %d must be between 1 and %d", port, Short.MAX_VALUE));
        return this;
    }

    /**
     * Overrides the current name of this service. Can be called multiple times.
     * 
     * @param serviceName The new name for this service
     * @return This builder to allow chaining
     * @throws IllegalArgumentException if the new service name is <code>null</code> or empty.
     */
    public Thrift named(String serviceName) {
        this.serviceName = checkNotEmpty(serviceName, "Must provide a non-empty service name");
        return this;
    }

    public Thrift serving(TProcessor resource) {
        tProcessorFactory = new TProcessorFactory(resource);
        return this;
    }

}
