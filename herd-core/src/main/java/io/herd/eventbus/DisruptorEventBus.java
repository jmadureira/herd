package io.herd.eventbus;

import io.herd.base.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

/**
 * An implementation of an {@link EventBus} that uses the {@link Disruptor} to pass events to subscribers.
 * 
 * @author joaomadureira
 *
 */
public class DisruptorEventBus implements EventBus, Service {

    public static void translate(Envelope targetEvent, long sequence, Envelope sourceEvent) {
        targetEvent.setData(sourceEvent.getData());
    }
    
    private static final Logger logger = LoggerFactory.getLogger(DisruptorEventBus.class);
    private final ExecutorService executor;
    private final int bufferSize;

    private final Disruptor<Envelope> disruptor;

    private volatile boolean running;

    public DisruptorEventBus() {
        this.executor = Executors.newCachedThreadPool();
        this.bufferSize = 1024;
        this.disruptor = new Disruptor<>(Envelope::new, bufferSize, executor, ProducerType.MULTI,
                new SleepingWaitStrategy());
        this.disruptor.handleExceptionsWith(new LoggingExceptionHandler(logger));
        this.running = false;
    }

    @Override
    public void emit(Object event) {
        if (isRunning()) {
            Envelope envelope = new Envelope();
            envelope.setData(event);
            RingBuffer<Envelope> ringBuffer = disruptor.getRingBuffer();
            ringBuffer.publishEvent(DisruptorEventBus::translate, envelope);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void start() {
        if (!isRunning()) {
            disruptor.start();
            this.running = true;
        }
    }
    
    @Override
    public void stop() {
        this.running = false;
        disruptor.shutdown();
        executor.shutdown();
    }

    @SuppressWarnings("unchecked")
    @Override
    public EventBus subscribe(Object subscriber) {
        if (isRunning()) {
            throw new IllegalStateException("Cannot add a new subscription while the event bus is running.");
        }
        try {
            this.disruptor.handleEventsWith(ReflectiveEventHandler.forSubscriber(subscriber));
        } catch(SubscriberException e) {
            logger.warn(e.toString());
        }
        return this;
    }

    @Override
    public EventBus unsubscribe(Object subscriber) {
        throw new UnsupportedOperationException("Cannot unsubscribe listeners at the moment.");
    }

}
