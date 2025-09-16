package com.instaclustr.kafka;

import com.instaclustr.kafka.forwarders.MetricsExporter;
import com.instaclustr.kafka.forwarders.MetricsExporterFactory;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class KafkaClientMetricsReporterReceiver implements ClientTelemetryReceiver {

    private final MetricsExporter metricsExporter;
    private final Map<String, Object> metadata;
    private static final Logger logger = LoggerFactory.getLogger(KafkaClientMetricsReporterReceiver.class);

    public KafkaClientMetricsReporterReceiver() {
        logger.info("Initializing the Kafka Client Metrics Reporter Receiver");
        final KafkaClientMetricsReporterConfig kafkaClientMetricsReporterConfig = new KafkaClientMetricsReporterConfig();
        this.metricsExporter = MetricsExporterFactory.create(kafkaClientMetricsReporterConfig.configurations);
        this.metadata = (Map<String, Object>) kafkaClientMetricsReporterConfig.configurations.get("metadata");
        logger.info("Initialized the Kafka Client Metrics Reporter Receiver");
    }

    @Override
    public void exportMetrics(AuthorizableRequestContext requestContext, ClientTelemetryPayload telemetryPayload) {
        metricsExporter.export(telemetryPayload);
    }
}

