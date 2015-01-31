package io.herd.base;


/**
 * Common interface for classes that act as services.
 *
 */
public interface Service {

    /**
     * Starts this service. Subsequent calls to this method should yield no results.
     *
     * @throws ServerRuntimeException if the service failed to start.
     * @see #stop()
     */
    void start();

    /**
     * Stops this service and release all resources it holds. Subsequent calls to this method should yield no results;
     * 
     * @see #start()
     */
    void stop();

    /**
     * Checks if this service is running. The general contract is that:
     * <ul>
     * <li>calling this method before {@link #start()} must always return <code>false</code></li>
     * <li>calling this method after {@link #start()} must return <code>true</code> iff the service started correctly</li>
     * <li>calling this method after {@link #stop()} must return <code>false</code> iff the service stopped correctly</li>
     * <li>no guarantees are given if this method is called while the service is starting or stopped</li>
     * </ul>
     * 
     * @return whether the service is running or not.
     */
    boolean isRunning();
}
