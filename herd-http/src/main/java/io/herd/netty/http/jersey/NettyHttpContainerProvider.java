package io.herd.netty.http.jersey;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.spi.ContainerProvider;

public class NettyHttpContainerProvider implements ContainerProvider {

    @Override
    public <T> T createContainer(Class<T> type, Application application) throws ProcessingException {
        if (type == NettyHttpContainer.class) {
            return type.cast(new NettyHttpContainer(application));
        }
        return null;
    }

}
