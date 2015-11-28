package io.herd.monitoring;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.lmax.disruptor.EventHandler;

public class DefaultEventHandler implements EventHandler<Event> {

    private static class CustomMetric implements TimedMetricMXBean {

        private final LongAdder counter = new LongAdder();

        @Override
        public long getCount() {
            return counter.sum();
        }

        @Override
        public void inc() {
            counter.increment();
        }

    }

    private final MBeanServer mbServer;

    private final ConcurrentHashMap<String, TimedMetricMXBean> metrics;

    public DefaultEventHandler() {
        this.mbServer = ManagementFactory.getPlatformMBeanServer();
        this.metrics = new ConcurrentHashMap<>();
    }

    @Override
    public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
        if (!metrics.containsKey(event.getId())) {
            ObjectName mxBeanName = new ObjectName("Herd:type=" + event.getId());

            CustomMetric customMetric = new CustomMetric();
            mbServer.registerMBean(customMetric, mxBeanName);
            metrics.put(event.getId(), customMetric);
        }

        TimedMetricMXBean metric = metrics.get(event.getId());

        metric.inc();
    }

}
