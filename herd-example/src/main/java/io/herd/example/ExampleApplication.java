package io.herd.example;

import io.herd.http.Http;
import io.herd.server.Application;
import io.herd.thrift.Thrift;

public class ExampleApplication extends Application {

    public static void main(String[] args) {
        new ExampleApplication().run(args);
    }

    @Override
    protected void initialize() {
        registerService(
                createService("HTTP Service", new Http(null)).listen(8080).serving(new ExampleResource()));
        
        registerService(
                createService("Thrift Servive", new Thrift()).listen(9090));
    }

}
