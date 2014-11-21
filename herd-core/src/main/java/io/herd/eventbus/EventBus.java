package io.herd.eventbus;

public interface EventBus {

    void emit(Object event);
    
    EventBus subscribe(Object subscriber);
    
    EventBus unsubscribe(Object subscriber);
}
