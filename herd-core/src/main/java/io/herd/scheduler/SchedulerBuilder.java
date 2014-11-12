package io.herd.scheduler;

import static io.herd.base.Preconditions.checkNotNull;
import static io.herd.base.Preconditions.checkPositive;
import io.herd.base.Reflections;
import io.herd.concurrent.DefaultThreadFactory;
import io.herd.monitoring.Emitter;
import io.herd.monitoring.Event;
import io.herd.monitoring.Timed;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SchedulerBuilder {

    private static class RunnableTask {
        final Runnable runnable;
        final int period;

        RunnableTask(int period, Runnable runnable) {
            this.period = period;
            this.runnable = runnable;
        }
    }

    private static final Logger logger = LogManager.getLogger(SchedulerBuilder.class);

    public static final int DEFAULT_POOL_SIZE = 2;

    private int poolSize;
    private ThreadFactory threadFactory;
    private List<RunnableTask> taskList = new LinkedList<>();
    private List<RunnableTask> delayedTaskList = new LinkedList<>();
    private final Emitter<Event> emitter;

    public SchedulerBuilder(Emitter<Event> emitter) {
        this(DEFAULT_POOL_SIZE, new DefaultThreadFactory(), emitter);
    }

    public SchedulerBuilder(int poolSize, Emitter<Event> emitter) {
        this(poolSize, new DefaultThreadFactory(), emitter);
    }

    public SchedulerBuilder(int poolSize, ThreadFactory threadFactory, Emitter<Event> emitter) {
        this.poolSize = poolSize;
        this.threadFactory = threadFactory;
        this.emitter = emitter;
    }

    public ExecutorService build() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(poolSize, threadFactory);
        for (RunnableTask task : taskList) {
            service.scheduleAtFixedRate(getTask(task.runnable), 1000, task.period, TimeUnit.MILLISECONDS);
        }
        for (RunnableTask task : delayedTaskList) {
            service.scheduleWithFixedDelay(getTask(task.runnable), 1000, task.period, TimeUnit.MILLISECONDS);
        }
        return service;
    }

    private Runnable getTask(final Runnable runnable) {
        if (emitter == null) {
            // might as well return since there's no emitter to emit the events anyway
            logger.warn("Not checking for timed task since there's no emitter registered.");
            return runnable;
        }
        Method runMethod = Reflections.findMethod("run", runnable.getClass());
        Timed timed = runMethod.getAnnotation(Timed.class);
        if (timed == null) {
            return runnable;
        }
        logger.debug("Wrapping timed task named {}", timed.name());
        return new Runnable() {

            @Override
            public void run() {
                Event event = new Event(timed.name()).start();
                try {
                    runnable.run();
                    emitter.emit(event.stop());
                } catch (Exception e) {
                    emitter.emit(event.stopWithFailure());
                    throw e;
                }
            }
        };
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
        this.taskList.add(new RunnableTask(period, runnable));
        return this;
    }

    /**
     * Registers a <code>runnable</code> that will execute a periodic action and subsequently with the given delay
     * between the termination of one execution and the commencement of the next. If any execution of the task
     * encounters an exception, subsequent executions are suppressed. Otherwise, the task will only terminate via
     * cancellation or termination of the executor.
     *
     * @param period the delay between the termination of one execution and the commencement of the next
     * @param runnable the task to execute
     * @return a ScheduledFuture representing pending completion of the task, and whose {@code get()} method will throw
     *         an exception upon cancellation
     * @return This builder to allow chaining.
     * @throws IllegalArgumentException if the period specified isn't valid (<= 0).
     * @throws NullPointerException if the runner is <code>null</code>.
     */
    public SchedulerBuilder scheduleWithFixedDelay(int period, Runnable runnable) {
        checkPositive(period, "Must specify a period > 0");
        checkNotNull(runnable, "Must specify a non null runnable");
        this.delayedTaskList.add(new RunnableTask(period, runnable));
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
