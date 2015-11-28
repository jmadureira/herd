package io.herd.server;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static io.herd.base.Preconditions.checkNotNull;
import static io.herd.base.Preconditions.checkState;
import io.herd.ServerRuntime;
import io.herd.base.Builder;
import io.herd.base.Configuration;
import io.herd.base.Reflections;
import io.herd.base.Service;
import io.herd.eventbus.DisruptorEventBus;
import io.herd.eventbus.EventBus;
import io.herd.gossip.Gossip;
import io.herd.gossip.GossipConfiguration;
import io.herd.http.HttpClient;
import io.herd.monitoring.DefaultEventEmitter;
import io.herd.monitoring.DefaultEventHandler;
import io.herd.scheduler.SchedulerBuilder;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Stage;

/**
 * Base class for all applications created using this framework.
 * 
 */
public abstract class Application<T extends Configuration> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final class ApplicationModule extends AbstractModule {

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
        protected void configure() {
            bind(ObjectMapper.class).toProvider(new Provider<ObjectMapper>() {

                @Override
                public ObjectMapper get() {
                    return new ObjectMapper().configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true).registerModule(
                            new AfterburnerModule());
                }

            }).in(Singleton.class);
            bind(EventBus.class).toProvider(new Provider<EventBus>() {

                @Override
                public EventBus get() {
                    eventBus = new DisruptorEventBus();
                    return eventBus;
                }
            });
        }
        
    }

    private ConfigurationFactory<T> configurationFactory;
    private final LinkedList<ServerRuntime> services;
    private final LinkedList<SchedulerBuilder> schedulers;
    private final DefaultEventEmitter eventEmitter;
    private final LinkedList<Function<T, Module>> modules;
    private EventBus eventBus;

    private final Application<T>.ApplicationModule appModule;
    private Injector injector;
    private T configuration;

    @SuppressWarnings("unchecked")
    protected Application() {
        this.services = new LinkedList<>();
        this.schedulers = new LinkedList<>();
        this.eventEmitter = new DefaultEventEmitter();
        this.eventEmitter.withEventHandlers(new DefaultEventHandler());
        this.modules = new LinkedList<>();
        this.appModule = new ApplicationModule();
     // add myself as a module as well
        this.modules.add((conf) -> this.appModule);
    }
    
    protected EventBus getApplicationEventBus() {
        if (eventBus == null) {
            eventBus = getResource(EventBus.class);
        }
        return eventBus;
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

    protected void registerService(Builder<ServerRuntime> serviceBuilder) {
        checkNotNull(serviceBuilder, "Cannot register a null service");
        this.services.add(serviceBuilder.build());
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down application.");
            stop();
        }));
    }

    /**
     * Application's main entry point. Should be given the command line arguments provided during startup.
     * 
     * @param args A list of arguments to pass to the application.
     * @throws NullPointerException if a <code>null args</code> is provided.
     */
    public void run(String[] args) {

        checkNotNull(args, "Must provide an non-null array of arguments.");

        if (args.length == 1) {
            this.configurationFactory = new ConfigurationFactory<>(Reflections.getTypeParameter(getClass(),
                    Configuration.class), args[0]);
        } else {
            this.configurationFactory = new ConfigurationFactory<>(Reflections.getTypeParameter(getClass(),
                    Configuration.class));
        }
        this.configuration = this.configurationFactory.build();

        try {
            this.injector = Guice.createInjector(Stage.PRODUCTION,
                    modules.stream().map((module) -> module.apply(this.configuration))
                    .collect(Collectors.toList()));

            this.injector.injectMembers(this);
        } catch (Exception e) {
            logger.error("Failed to create an injector.", e);
            return;
        }

        /*
         * initialize internal services if necessary
         */
        // start with gossiper
        initalizeGossiper(this.configuration.getGossiper());

        initialize(this.configuration);

        if (startServices()) {
            registerShutdownHook();
        } else {
            stop();
        }
    }
    
    public Application<T> install(Function<T, Module> installHandler) {
        this.modules.add(installHandler);
        return this;
    }

    private final void initalizeGossiper(GossipConfiguration gossiper) {
        if (gossiper.isEnabled()) {
            logger.info("Service {} is enabled", gossiper.getServiceName());
            registerService(new Gossip(gossiper));
        } else {
            logger.info("Service {} is disabled", gossiper.getServiceName());
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
