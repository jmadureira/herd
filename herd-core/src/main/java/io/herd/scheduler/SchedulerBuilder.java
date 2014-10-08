package io.herd.scheduler;

import static io.herd.base.Preconditions.checkNotNull;
import static io.herd.base.Preconditions.checkPositive;
import io.herd.concurrent.DefaultThreadFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class SchedulerBuilder {

    private static class Task {
        final Runnable runnable;
        final int period;

        Task(int period, Runnable runnable) {
            this.period = period;
            this.runnable = runnable;
        }
    }

    public static final int DEFAULT_POOL_SIZE = 2;

    private int poolSize;
    private ThreadFactory threadFactory;
    private List<Task> runnable = new LinkedList<>();

    public SchedulerBuilder() {
        this(DEFAULT_POOL_SIZE, new DefaultThreadFactory());
    }

    public SchedulerBuilder(int poolSize) {
        this(poolSize, new DefaultThreadFactory());
    }

    public SchedulerBuilder(int poolSize, ThreadFactory threadFactory) {
        this.poolSize = poolSize;
        this.threadFactory = threadFactory;
    }

    public ExecutorService build() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(poolSize, threadFactory);
        for (Task task : runnable) {
            service.scheduleAtFixedRate(task.runnable, 1000, task.period, TimeUnit.MILLISECONDS);
        }
        return service;
    }

    /**
     * Registers a <code>runnable</code> that will be executed periodically within the given period; that is executions
     * will commence after <code>period</code>, then 2 * <code>period</code>, and so on. If any execution of the task
     * encounters an exception, subsequent executions are suppressed. Otherwise, the <code>runnable</code> will only
     * terminate via cancellation or termination of the executor. If any execution of the <code>runnable</code> takes
     * longer than its period, then subsequent executions may start late, but will not concurrently execute.
     * 
     * @param period The period between executions.
     * @param runnable The runnable being executed periodically.
     * @return This builder to allow chaining.
     * @throws IllegalArgumentException if the period specified isn't valid (<= 0).
     * @throws NullPointerException if the runner is <code>null</code>.
     */
    public SchedulerBuilder schedule(int period, Runnable runnable) {
        checkPositive(period, "Must specify a period > 0");
        checkNotNull(runnable, "Must specify a non null runnable");
        this.runnable.add(new Task(period, runnable));
        return this;
    }

    /**
     * Specifies the number of threads that will be kept in the pool even if idle.
     * 
     * @param poolSize A positive integer.
     * @return This builder to allow chaining.
     * @throws IllegalArgumentException if the poolSize specified isn't valid (<= 0).
     */
    public SchedulerBuilder withPoolSize(int poolSize) {
        checkPositive(poolSize, "Must specify a pool size > 0.");
        this.poolSize = poolSize;
        return this;
    }

    /**
     * Specifies a custom {@link ThreadFactory} to be used by the scheduler. Has no effect if <code>null</code> is
     * provided.
     * 
     * @param threadFactory A {@link ThreadFactory} to be used by the scheduler being built.
     * @return This builder to allow chaining.
     */
    public SchedulerBuilder withThreadFactory(ThreadFactory threadFactory) {
        if (threadFactory != null) {
            this.threadFactory = threadFactory;
        }
        return this;
    }
}
