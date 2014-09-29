package io.herd.concurrent;

import static io.herd.base.Preconditions.checkNotEmpty;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a {@link ThreadFactory} that allows some common configurations like setting the name of the
 * created threads, if they run as daemons, etc.
 * 
 * @author joaomadureira
 *
 */
public class DefaultThreadFactory implements ThreadFactory {

    private static final String DEFAULT_THREAD_NAME = "thread-";

    private static final Logger logger = LoggerFactory.getLogger(DefaultThreadFactory.class);

    private final AtomicLong threadIncrement = new AtomicLong(0);
    private String threadName;
    private boolean isDaemon;

    /**
     * Creates a new {@link ThreadFactory} that will create non daemon threads named {@value #DEFAULT_THREAD_NAME}
     * [thread number].
     */
    public DefaultThreadFactory() {
        this(DEFAULT_THREAD_NAME, false);
    }

    /**
     * Creates a new {@link ThreadFactory} that will create non daemon threads with the specified thread name followed
     * by its number.
     * 
     * @param threadName What to call each newly created thread.
     * @throws NullPointerException if the <code>threadName</code> is null.
     * @throws IllegalArgumentException if the <code>threadName</code> is empty.
     */
    public DefaultThreadFactory(String threadName) {
        this(threadName, false);
    }

    /**
     * Creates a new {@link ThreadFactory} whose thread's names will have the specified <code>threadName</code> followed
     * by its number.
     * 
     * @param threadName What to call each newly created thread.
     * @param isDaemon Whether the created threads will run as daemon threads or not.
     * @throws NullPointerException if the <code>threadName</code> is null.
     * @throws IllegalArgumentException if the <code>threadName</code> is empty.
     */
    public DefaultThreadFactory(String threadName, boolean isDaemon) {
        checkNotEmpty(threadName, "The thread factory requires a non-empty threadName.");
        this.threadName = threadName;
        this.isDaemon = isDaemon;
    }

    @Override
    public Thread newThread(Runnable r) {

        String tName = threadName + threadIncrement.getAndIncrement();
        logger.debug("Creating thread {}.", tName);

        Thread t = new Thread(r);
        t.setName(tName);
        t.setDaemon(isDaemon);
        return t;
    }

    /**
     * Sets whether each newly created thread will run as daemon as opposed to a user thread. Basically this tells the
     * JVM if it should wait for these threads to finish before shutting exiting.
     * 
     * @param isDaemon Whether each newly created thread should run as a daemon or user thread.
     * @return This instance.
     */
    public DefaultThreadFactory setDaemon(boolean isDaemon) {
        this.isDaemon = isDaemon;
        return this;
    }

}
