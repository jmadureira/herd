package io.herd.example;


import io.herd.http.Http;
import io.herd.http.HttpModule;
import io.herd.server.Application;
import io.herd.thrift.Thrift;
import io.herd.thrift.ThriftModule;

import com.google.inject.Inject;

public class ExampleApplication extends Application<ExampleConfiguration> {
    
    @Inject
    private Thrift thrift;
    
    @Inject
    private Http http;

    public static void main(String[] args) {
        new ExampleApplication()
                .install((conf) -> new ThriftModule(conf.getThriftFactory()))
                .install((conf) -> new HttpModule(conf.getServerFactory()))
                .run(args);
    }

    @Override
    protected void initialize(ExampleConfiguration configuration) {
        registerService(thrift);
        registerService(http);
        http.get("/", (req) -> {
            req.setContent("Hello");
        });
    }

}
