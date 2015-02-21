package io.herd.thrift;

import static org.junit.Assert.assertNotNull;
import io.herd.ServerRuntime;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ThriftTest {

    @Test
    public void testBuildWithDefaultConfiguration() {
        Injector injector = Guice.createInjector(new ThriftModule());
        Thrift thrift = injector.getInstance(Thrift.class);
        ServerRuntime serverRuntime = thrift.build();
        assertNotNull(serverRuntime);
    }
    
    @Test
    public void testBuildWithNullConfiguration() {
        Injector injector = Guice.createInjector(new ThriftModule(null));
        Thrift thrift = injector.getInstance(Thrift.class);
        ServerRuntime serverRuntime = thrift.build();
        assertNotNull(serverRuntime);
    }
    
    @Test
    public void testBuildWithCustomConfiguration() {
        DefaultThriftConfiguration conf = new DefaultThriftConfiguration();
        conf.setPort(8080);
        Injector injector = Guice.createInjector(new ThriftModule(conf));
        Thrift thrift = injector.getInstance(Thrift.class);
        ServerRuntime serverRuntime = thrift.build();
        assertNotNull(serverRuntime);
    }
    
    @Test
    public void testBuildWithOverrideConfiguration() {
        DefaultThriftConfiguration conf = new DefaultThriftConfiguration();
        Injector injector = Guice.createInjector(new ThriftModule(conf));
        Thrift thrift = injector.getInstance(Thrift.class).listen(8080).named("Custom thrift");
        ServerRuntime serverRuntime = thrift.build();
        assertNotNull(serverRuntime);
    }
}
