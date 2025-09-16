package com.instaclustr.kafka.exporters;

import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogMetricsExporter implements MetricsExporter {
    private final String logPath;
    private final Logger logger = LoggerFactory.getLogger(LogMetricsExporter.class);

    public LogMetricsExporter(String logPath) {
        this.logPath = logPath != null ? logPath : "/lib/default-metrics.log";
    }

    @Override
    public void export(ClientTelemetryPayload payload) {
        logger.info("Logging metrics to {}: {}", logPath, payload);
    }
}
