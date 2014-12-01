package io.herd.base;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link StreamProvider} implementation that returns an {@link InputStream} to a resource found through a
 * {@link ClassLoader}.
 * 
 * @author joaomadureira
 *
 */
public class ResourceProvider implements StreamProvider {

    private final String classpath;
    private final ClassLoader classLoader;

    public ResourceProvider(String classpath) {
        this(classpath, Thread.currentThread().getContextClassLoader());
    }

    public ResourceProvider(String classpath, ClassLoader classLoader) {
        this.classpath = classpath;
        this.classLoader = classLoader;
    }

    @Override
    public InputStream open() throws IOException {
        InputStream resource = classLoader.getResourceAsStream(classpath);
        if (resource == null) {
            throw new IOException(classpath + " resource was not found on classpath");
        }
        return resource;
    }

}
