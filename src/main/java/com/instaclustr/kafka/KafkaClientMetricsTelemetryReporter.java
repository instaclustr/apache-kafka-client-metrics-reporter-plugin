package com.instaclustr.kafka;

import java.util.List;
import java.util.Map;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.apache.kafka.common.metrics.MetricsReporter;
import org.apache.kafka.server.telemetry.ClientTelemetry;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaClientMetricsTelemetryReporter implements MetricsReporter, ClientTelemetry {

    private static final Logger logger = LoggerFactory.getLogger(KafkaClientMetricsTelemetryReporter.class);

    private KafkaClientMetricsTelemetryConfig kafkaClientMetricsTelemetryConfig;

    @Override
    public void init(List<KafkaMetric> metrics) {
        logger.info("Initializing the client metric reporter: {}", metrics);
    }

    @Override
    public void configure(final Map<String, ?> configs) {
        final String kafkaClientMetricsConfigFilePath = System.getenv("KAFKA_CLIENT_METRICS_CONFIG_PATH");
        if (kafkaClientMetricsConfigFilePath == null || kafkaClientMetricsConfigFilePath.isEmpty()) {
            logger.error("KAFKA_CLIENT_METRICS_CONFIG_PATH environment variable not set");
        } else {
            this.kafkaClientMetricsTelemetryConfig = KafkaClientMetricsTelemetryConfig.load(kafkaClientMetricsConfigFilePath);
        }
    }

    @Override
    public void metricChange(final KafkaMetric metric) {
        logger.info("Changing the metric {}", metric.metricName());
    }

    @Override
    public void metricRemoval(final KafkaMetric metric) {
        logger.info("Removing the metric {}", metric.metricName());
    }

    @Override
    public void close() {
        logger.info("Closing the reporter");
    }

    @Override
    public ClientTelemetryReceiver clientReceiver() {
        if (this.kafkaClientMetricsTelemetryConfig == null) {
            logger.info("Reporter not configured. Call configure() first.");
        }
        return new KafkaClientMetricsTelemetryReceiver(this.kafkaClientMetricsTelemetryConfig);
    }
}