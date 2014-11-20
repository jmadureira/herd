package io.herd.eventbus;

import org.slf4j.Logger;

import com.lmax.disruptor.ExceptionHandler;

/**
 * Simple implementation of an {@link ExceptionHandler} that simply logs the exception to a {@link Logger};
 * 
 * @author joaomadureira
 *
 */
public class LoggingExceptionHandler implements ExceptionHandler {

    private final Logger logger;

    public LoggingExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        logger.error("Unable to process item {} due to {}", event, ex.toString());
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        logger.error("Failed to stop event bus due to {}", ex.toString());
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        logger.error("Failed to start event bus due to {}", ex.toString());
    }

}
