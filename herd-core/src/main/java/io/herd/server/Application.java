package io.herd.server;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static io.herd.base.Preconditions.checkNotNull;
import static io.herd.base.Preconditions.checkState;
import io.herd.ServerRuntime;
import io.herd.Service;
import io.herd.base.Configuration;
import io.herd.base.Reflections;
import io.herd.eventbus.DisruptorEventBus;
import io.herd.eventbus.EventBus;
import io.herd.http.HttpClient;
import io.herd.http.NingHttpClient;
import io.herd.monitoring.DefaultEventEmitter;
import io.herd.monitoring.DefaultEventHandler;
import io.herd.netty.NettyServerRuntime;
import io.herd.netty.Transport;
import io.herd.scheduler.SchedulerBuilder;

import java.util.LinkedList;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
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
public abstract class Application<T extends Configuration> extends AbstractModule {

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

    private ConfigurationFactory<T> configurationFactory;
    private final LinkedList<ServerRuntime> services;
    private final LinkedList<SchedulerBuilder> schedulers;
    private final DefaultEventEmitter eventEmitter;
    private EventBus eventBus;

    private Injector injector;
    private T configuration;

    @SuppressWarnings("unchecked")
    protected Application() {
        this.services = new LinkedList<>();
        this.schedulers = new LinkedList<>();
        this.eventEmitter = new DefaultEventEmitter();
        this.eventEmitter.withEventHandlers(new DefaultEventHandler());
    }

    /**
     * Performs the defaults bindings of the {@link Application} that can be used by clients. At the moment the
     * following bindings are available:
     * <ul>
     * <li>a {@link Singleton} binding for an {@link ObjectMapper};</li>
     * <li>a default implementation of an {@link EventBus};</li>
     * <li>a {@link Singleton} binding for an {@link HttpClient};</li>
     * </ul>
     * To extend the application configuration users should override the {@link #configureBindings(Configuration)} and
     * add their own bindings.
     * 
     * @see #configureBindings(Configuration)
     */
    @Override
    protected final void configure() {
        bind(ObjectMapper.class).toProvider(
                () -> new ObjectMapper().configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true).registerModule(
                        new AfterburnerModule())).in(Singleton.class);
        bind(EventBus.class).toProvider(() -> {
            eventBus = new DisruptorEventBus();
            return eventBus;
        });
        bind(HttpClient.class).to(NingHttpClient.class).in(Singleton.class);
        configureBindings(this.configuration);
    }

    protected void configureBindings(T configuration) {
    }

    protected <Resource> ServiceBuilder<Resource> createService(String service, Transport<Resource> transport) {
        return new ServiceBuilder<Resource>(service, transport);
    }

    protected EventBus getApplicationEventBus() {
        if (eventBus == null) {
            eventBus = getResource(EventBus.class);
        }
        return eventBus;
    }

    protected ServiceBuilder<?> createService(Transport<?> transport) {
        return createService(transport.getName(), transport);
    }

    protected SchedulerBuilder createScheduler() {
        SchedulerBuilder schedulerBuilder = new SchedulerBuilder(this.eventEmitter);
        schedulers.add(schedulerBuilder);
        return schedulerBuilder;
    }

    /**
     * Returns an instance of type <code>R</code> previously registered within the framework.
     * 
     * @param resourceType
     * @return An instance of type R.
     * @throws IllegalArgumentException if this method is called before the application is initialized.
     */
    protected <R> R getResource(Class<R> resourceType) {
        checkState(this.injector != null, "Cannot call getResource() before initializing the app.");
        return this.injector.getInstance(resourceType);
    }

    /**
     * Initialized the application. This will be where users configure all aspects of the application.
     */
    protected abstract void initialize(T configuration);

    protected void registerService(ServiceBuilder<?> service) {
        this.services.add(service.build());
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                logger.info("Shutting down application.");
                stop();
            }
        }));
    }

    /**
     * Application's main entry point. Should be given the command line arguments provided during startup.
     * 
     * @param args A list of arguments to pass to the application.
     * @throws NullPointerException if a <code>null args</code> is provided.
     */
    protected void run(String[] args) {

        checkNotNull(args, "Must provide an non-null array of arguments.");
        
        if(args.length == 1) {
            this.configurationFactory = new ConfigurationFactory<>(Reflections.getTypeParameter(getClass(), Configuration.class), args[0]);
            this.configuration = this.configurationFactory.build();
        } else {
            this.configurationFactory = new ConfigurationFactory<>(Reflections.getTypeParameter(getClass(), Configuration.class));
            this.configuration = this.configurationFactory.build();
        }

        this.injector = Guice.createInjector(Stage.PRODUCTION, this);

        initialize(this.configuration);

        if (startServices()) {
            registerShutdownHook();
        } else {
            stop();
        }
    }

    private boolean startServices() {
        try {
            this.eventEmitter.start();
            if (eventBus != null && eventBus instanceof Service) {
                ((Service) eventBus).start();
            }
            for (Service service : services) {
                service.start();
            }
            for (SchedulerBuilder service : schedulers) {
                service.build();
            }
            return true;
        } catch (Exception e) {
            logger.error("Failed to start service due to {}", e.toString());
            return false;
        }
    }

    private void stop() {
        if (eventBus != null && eventBus instanceof Service) {
            ((Service) eventBus).stop();
        }
        for (ServerRuntime server : services) {
            if (server.isRunning()) {
                server.stop();
            }
        }
        this.eventEmitter.stop();
    }
}
