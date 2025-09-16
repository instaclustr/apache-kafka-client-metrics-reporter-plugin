package com.instaclustr.kafka;

import java.util.List;
import java.util.Map;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.apache.kafka.common.metrics.MetricsReporter;
import org.apache.kafka.server.telemetry.ClientTelemetry;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaClientMetricsReporter implements MetricsReporter, ClientTelemetry {

    private static final Logger logger = LoggerFactory.getLogger(KafkaClientMetricsReporter.class);


    @Override
    public void init(List<KafkaMetric> metrics) {
        logger.debug("Initializing the client metric reporter: {}", metrics);
    }

    @Override
    public void configure(final Map<String, ?> configs) {
        logger.debug("Configuration of the reporter");
    }

    @Override
    public void metricChange(final KafkaMetric metric) {
        logger.debug("Changing the metric {}", metric.metricName());
    }

    @Override
    public void metricRemoval(final KafkaMetric metric) {
        logger.debug("Removing the metric {}", metric.metricName());
    }

    @Override
    public void close() {
        logger.debug("Closing the reporter");
    }

    @Override
    public ClientTelemetryReceiver clientReceiver() {
        return new KafkaClientMetricsReporterReceiver();
    }
}