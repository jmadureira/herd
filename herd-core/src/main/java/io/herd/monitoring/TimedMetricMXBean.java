package io.herd.monitoring;

public interface TimedMetricMXBean {

    long getCount();

    void inc();
}
