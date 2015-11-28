package io.herd.monitoring;


public class Event {

    private String id;
    private boolean failure;
    private long elapsedTime;
    private long startTime;
    
    public Event() {
        
    }

    public Event(String id) {
        this.id = id;
    }

    public void copyEvent(Event otherEvent) {
        this.id = otherEvent.id;
        this.startTime = otherEvent.startTime;
        this.elapsedTime = otherEvent.elapsedTime;
        this.failure = otherEvent.failure;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public String getId() {
        return id;
    }

    public boolean isFailure() {
        return failure;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public void setFailure(boolean failure) {
        this.failure = failure;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Event start() {
        this.startTime = System.nanoTime();
        return this;
    }
    
    public Event stop() {
        this.elapsedTime = System.nanoTime() - this.startTime;
        this.failure = false;
        return this;
    }

    public Event stopWithFailure() {
        this.elapsedTime = System.nanoTime() - this.startTime;
        this.failure = true;
        return this;
    }

}
