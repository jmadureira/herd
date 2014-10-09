package io.herd.monitoring;

public class Event {

    private String id;
    private boolean failure;
    private double elapsedTime;

    public Event() {

    }

    public Event(String id, double elapsedTime, boolean isFailure) {
        this.id = id;
        this.elapsedTime = elapsedTime;
        this.failure = isFailure;
    }

    public void copyEvent(Event otherEvent) {
        this.id = otherEvent.id;
        this.elapsedTime = otherEvent.elapsedTime;
        this.failure = otherEvent.failure;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public String getId() {
        return id;
    }

    public boolean isFailure() {
        return failure;
    }

    public void setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public void setFailure(boolean failure) {
        this.failure = failure;
    }

    public void setId(String id) {
        this.id = id;
    }

}
