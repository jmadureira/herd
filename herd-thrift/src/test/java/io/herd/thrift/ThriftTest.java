package io.herd.thrift;

import static org.junit.Assert.assertNotNull;
import io.herd.ServerRuntime;

import org.junit.Test;

public class ThriftTest {

    @Test
    public void testBuildWithDefaultConfiguration() {
        Thrift thrift = new Thrift();
        ServerRuntime serverRuntime = thrift.build();
        assertNotNull(serverRuntime);
    }
    
    @Test
    public void testBuildWithNullConfiguration() {
        Thrift thrift = new Thrift(null);
        ServerRuntime serverRuntime = thrift.build();
        assertNotNull(serverRuntime);
    }
    
    @Test
    public void testBuildWithCustomConfiguration() {
        DefaultThriftConfiguration conf = new DefaultThriftConfiguration();
        conf.setPort(8080);
        Thrift thrift = new Thrift(conf);
        ServerRuntime serverRuntime = thrift.build();
        assertNotNull(serverRuntime);
    }
    
    @Test
    public void testBuildWithOverrideConfiguration() {
        Thrift thrift = new Thrift(new DefaultThriftConfiguration()).listen(8080).named("Custom thrift");
        ServerRuntime serverRuntime = thrift.build();
        assertNotNull(serverRuntime);
    }
}
