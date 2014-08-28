package io.herd.server;

import static io.herd.base.Preconditions.*;
import io.herd.ServerRuntime;
import io.herd.Service;
import io.herd.netty.NettyServerRuntime;
import io.herd.netty.Transport;

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * Base class for all applications created using this framework.
 * 
 * @author joaomadureira
 *
 */
public abstract class Application extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static class ServiceBuilder<Resource> {

        private final String serviceName;
        private final Transport<Resource> transport;
        private int port;

        public ServiceBuilder(String serviceName, Transport<Resource> transport) {
            this.serviceName = serviceName;
            this.transport = transport;
        }

        public ServerRuntime build() {
            NettyServerRuntime server = new NettyServerRuntime(serviceName, transport);
            server.setPort(port);
            return server;
        }

        public ServiceBuilder<Resource> listen(int port) {
            this.port = port;
            return this;
        }

        public ServiceBuilder<Resource> serving(Resource resource) {
            transport.serving(resource);
            return this;
        }

    }

    private final LinkedList<ServerRuntime> services;

    private Injector injector;

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
     * Returns an instance of type <code>T</code> previously registered within the framework.
     * 
     * @param resourceType
     * @return An instance of type T.
     * @throws IllegalArgumentException if this method is called before the application is initialized.
     */
    protected <T> T getResource(Class<T> resourceType) {
        checkState(this.injector != null, "Cannot call getResource() before initializing the app.");
        return this.injector.getInstance(resourceType);
    }

    /**
     * Initialized the application. This will be where users configure all aspects of the application.
     */
    protected abstract void initialize();

    protected void registerService(ServiceBuilder<?> service) {
        this.services.add(service.build());
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                stop();
            }
        }));
    }

    /**
     * Application's main entry point. Should be given the command line arguments provided during startup.
     * 
     * @param args A list of arguments to pass to the application.
     * @throws NullPointerException if a <code>null</code> args is provided.
     */
    protected void run(String[] args) {

        checkNotNull(args, "Must provide an non-null array of arguments.");

        this.injector = Guice.createInjector(Stage.PRODUCTION, this);

        initialize();

        if (startServices()) {
            registerShutdownHook();
        } else {
            stop();
        }
    }

    private boolean startServices() {
        try {
            for (Service service : services) {
                service.start();
            }
            return true;
        } catch (Exception e) {
            logger.error("Failed to start service due to {}", e.toString());
            return false;
        }
    }

    private void stop() {
        for (ServerRuntime server : services) {
            if (server.isRunning()) {
                server.stop();
            }
        }
    }
}
