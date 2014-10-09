package io.herd.monitoring;

import io.herd.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

public class DefaultEventEmitter implements Emitter<Event>, Service {

    private static final class DefaultExceptionHandler implements ExceptionHandler {

        @Override
        public void handleEventException(Throwable ex, long sequence, Object event) {
            logger.error("Unable to process item {} due to {}", event, ex.toString());
        }

        @Override
        public void handleOnShutdownException(Throwable ex) {
            logger.error("Failed to stop event emitter due to {}", ex.toString());
        }

        @Override
        public void handleOnStartException(Throwable ex) {
            logger.error("Failed to start event emitter due to {}", ex.toString());
        }
    }

    private static void translate(Event targetEvent, long sequence, Event sourceEvent) {
        targetEvent.copyEvent(sourceEvent);
    }

    private static final Logger logger = LogManager.getLogger(DefaultEventEmitter.class);

    private final ExecutorService executor;
    private final int bufferSize;
    private final Disruptor<Event> disruptor;
    private volatile boolean running;

    public DefaultEventEmitter() {
        this.executor = Executors.newCachedThreadPool();
        this.bufferSize = 1024;
        this.disruptor = new Disruptor<Event>(Event::new, bufferSize, executor, ProducerType.MULTI,
                new SleepingWaitStrategy());
        this.disruptor.handleExceptionsWith(new DefaultExceptionHandler());
        this.running = false;
    }

    @Override
    public void emit(Event event) {
        if (isRunning()) {
            RingBuffer<Event> ringBuffer = disruptor.getRingBuffer();
            ringBuffer.publishEvent(DefaultEventEmitter::translate, event);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public void start() {
        disruptor.start();
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
        disruptor.shutdown();
        executor.shutdown();
    }

    @SuppressWarnings("unchecked")
    public DefaultEventEmitter withEventHandlers(final EventHandler<? super Event>... handlers) {
        this.disruptor.handleEventsWith(handlers);
        return this;
    }

}
