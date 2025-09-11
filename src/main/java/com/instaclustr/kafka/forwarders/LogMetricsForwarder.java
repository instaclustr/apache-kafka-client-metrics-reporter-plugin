package com.instaclustr.kafka.forwarders;

import com.instaclustr.kafka.KafkaClientMetricsTelemetryPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogMetricsForwarder implements MetricsForwarder {
    private final String logPath;
    private final Logger logger = LoggerFactory.getLogger(LogMetricsForwarder.class);

    public LogMetricsForwarder(String logPath) {
        this.logPath = logPath != null ? logPath : "/lib/default-metrics.log";
    }

    @Override
    public void forward(KafkaClientMetricsTelemetryPayload payload) {
        logger.info("Logging metrics to {}: {}", logPath, payload);
        // Logging logic here
    }
}
