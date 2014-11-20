package io.herd.eventbus;

public interface EventBus {

    void emit(Object event);
    
    void subscribe(Object subscriber);
    
    void unsubscribe(Object subscriber);
}
