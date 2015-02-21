package io.herd.thrift;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class ThriftModule extends AbstractModule {

    private final ThriftConfiguration thriftConfiguration;

    public ThriftModule() {
        this.thriftConfiguration = null;
    }

    public ThriftModule(ThriftConfiguration thriftConfiguration) {
        this.thriftConfiguration = thriftConfiguration;
    }

    @Override
    protected void configure() {
    }

    @Provides
    protected Thrift thrift() {
        return new Thrift(thriftConfiguration);
    }

}
