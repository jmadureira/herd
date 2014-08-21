package io.herd.server;

import static io.herd.base.Preconditions.checkNotNull;
import io.herd.ServerRuntime;
import io.herd.netty.NettyServerRuntime;
import io.herd.netty.Transport;

import java.util.LinkedList;

/**
 * Base class for all applications created using this framework.
 * 
 * @author joaomadureira
 *
 */
public abstract class Application {

    public static class ServiceBuilder<Resource> {

        private final String serviceName;
        private final Transport<Resource> transport;
        private int port;

        public ServiceBuilder(String serviceName, Transport<Resource> transport) {
            this.serviceName = serviceName;
            this.transport = transport;
        }

        public ServerRuntime build() {
            return new NettyServerRuntime(serviceName, transport);
        }

        public ServiceBuilder<Resource> listen(int port) {
            this.port = port;
            return this;
        }

    }

    private final LinkedList<ServerRuntime> services;

    protected Application() {
        this.services = new LinkedList<ServerRuntime>();
    }

    protected <Resource> ServiceBuilder<Resource> createService(String service, Transport<Resource> transport) {
        return new ServiceBuilder<Resource>(service, transport);
    }

    protected ServiceBuilder<?> createService(Transport<?> transport) {
        return createService(transport.getName(), transport);
    }

    /**
     * Initialized the application. This will be where users configure all aspects of the application.
     */
    protected abstract void initialize();

    protected void registerService(ServiceBuilder<?> service) {
        this.services.add(service.build());
    }

    /**
     * Application's main entry point. Should be given the command line arguments provided during startup.
     * 
     * @param args A list of arguments to pass to the application.
     */
    protected void run(String[] args) {
        checkNotNull(args, "Must provide an non-null array of arguments.");
        initialize();
    }
}
