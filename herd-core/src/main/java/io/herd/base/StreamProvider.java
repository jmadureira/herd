package io.herd.base;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provider interface that allows callers to retrieve an {@link InputStream} without any knowledge of where it came from
 * or out it is being handled.
 * 
 * @author joaomadureira
 * 
 */
public interface StreamProvider {

    /**
     * Opens the {@link InputStream} and returns it. It is the caller's responsibility to close the {@link InputStream}
     * when finished (or use try with resources, etc).
     * 
     * @throws IOException if unable to return a valid {@link InputStream}. <code>null</code> should never be returned.
     */
    InputStream open() throws IOException;
}
