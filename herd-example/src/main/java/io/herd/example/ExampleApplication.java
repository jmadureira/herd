package io.herd.example;

import io.herd.http.Http;
import io.herd.server.Application;
import io.herd.thrift.Thrift;

public class ExampleApplication extends Application<ExampleConfiguration> {

    public static void main(String[] args) {
        new ExampleApplication().run(args);
    }

    @Override
    protected void initialize(ExampleConfiguration configuration) {
        registerService(new Http(configuration.getServerFactory())
                .listen(configuration.getServerFactory().getPort())
                .serving(new ExampleResource()));
        
        registerService(new Thrift(configuration.getThriftFactory()));
    }

}
