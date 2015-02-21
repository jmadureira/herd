package io.herd.example;

import io.herd.http.Http;
import io.herd.server.Application;
import io.herd.thrift.Thrift;
import io.herd.thrift.ThriftModule;

public class ExampleApplication extends Application<ExampleConfiguration> {
    
    @Override
    protected void configureBindings(ExampleConfiguration configuration) {
        install(new ThriftModule(configuration.getThriftFactory()));
    }

    public static void main(String[] args) {
        new ExampleApplication().run(args);
    }

    @Override
    protected void initialize(ExampleConfiguration configuration) {
        registerService(new Http(configuration.getServerFactory())
                .listen(configuration.getServerFactory().getPort())
                .serving(new ExampleResource()));
        
        registerService(getResource(Thrift.class));
    }

}
