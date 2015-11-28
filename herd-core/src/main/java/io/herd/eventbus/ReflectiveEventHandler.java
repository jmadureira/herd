package io.herd.eventbus;

import static io.herd.base.Preconditions.checkState;
import io.herd.base.Reflections;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;

/**
 * {@link EventHandler} implementation that process events by forwarding them to a subscriber using reflection.
 * 
 * @author joaomadureira
 *
 */
public class ReflectiveEventHandler implements EventHandler<Envelope> {

    private static final Logger logger = LoggerFactory.getLogger(ReflectiveEventHandler.class);

    private final Object subscriber;
    private final MethodHandle handle;
    private final Class<?> messageType;

    public ReflectiveEventHandler(Object subscriber, MethodHandle handle, Class<?> messageType) {
        this.subscriber = subscriber;
        this.handle = handle;
        this.messageType = messageType;
    }

    @Override
    public void onEvent(Envelope event, long sequence, boolean endOfBatch) throws Exception {
        Object data = event.getData();
//        logger.info("Received event {}", data);
        if (messageType.isInstance(data)) {
            try {
                handle.invoke(subscriber, data);
            } catch (Throwable e) {
                logger.error("Failed to process message. Reason was: {}", e.toString());
            }
        }
    }

    public static final EventHandler<? super Envelope> forSubscriber(Object subscriber) {
        Method method = Reflections.findAnnotatedMethod(Subscribe.class, subscriber.getClass());
        if (method == null) {
            throw new SubscriberException(subscriber + " doesn't have any method with @Subscribe.");
        }
        MethodHandle handle = getMethodHandle(subscriber, method);
        Class<?> argument = method.getParameters()[0].getType();
        return new ReflectiveEventHandler(subscriber, handle, argument);
    }
    
    private static MethodHandle getMethodHandle(Object subscriber, Method method) {
        checkState(method.getParameterCount() == 1, "Subscriber method must only accept a single argument");
        return Reflections.asMethodHandle(method);
    }
}
