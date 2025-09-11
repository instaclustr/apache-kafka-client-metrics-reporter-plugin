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

    private String configPath;
    private KafkaClientMetricsTelemetryConfig kafkaClientMetricsTelemetryConfig;

    @Override
    public void init(List<KafkaMetric> metrics) {
        logger.info("Initializing the KIP-714 metric reporter: " + metrics);
    }

    @Override
    public void configure(Map<String, ?> configs) {
        configPath = System.getenv("KAFKA_TELEMETRY_CONFIG_PATH");
        kafkaClientMetricsTelemetryConfig = KafkaClientMetricsTelemetryConfig.load(configPath);
        logger.info("Loaded telemetry config: {}", kafkaClientMetricsTelemetryConfig);
    }

    @Override
    public void metricChange(KafkaMetric metric) {
        logger.debug("Changing the metric: " + metric.metricName());
    }

    @Override
    public void metricRemoval(KafkaMetric metric) {
        logger.debug("Removing the metric: " + metric.metricName());
    }

    @Override
    public void close() {
        logger.debug("Closing the KIP-714 metric reporter");
    }

    @Override
    public ClientTelemetryReceiver clientReceiver() {
        return new KafkaClientMetricsTelemetryReceiver(kafkaClientMetricsTelemetryConfig);
    }
}