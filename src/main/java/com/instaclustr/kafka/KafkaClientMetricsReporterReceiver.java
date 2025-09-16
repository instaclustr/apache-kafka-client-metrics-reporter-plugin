package com.instaclustr.kafka;

import com.instaclustr.kafka.exporters.MetricsExporter;
import com.instaclustr.kafka.exporters.MetricsExporterFactory;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaClientMetricsReporterReceiver implements ClientTelemetryReceiver {

    private final MetricsExporter metricsExporter;
    private static final Logger logger = LoggerFactory.getLogger(KafkaClientMetricsReporterReceiver.class);

    public KafkaClientMetricsReporterReceiver() {
        logger.info("Initializing the Kafka Client Metrics Reporter Receiver");
        final KafkaClientMetricsReporterConfig kafkaClientMetricsReporterConfig = new KafkaClientMetricsReporterConfig();
        this.metricsExporter = MetricsExporterFactory.create(kafkaClientMetricsReporterConfig.configurations);
        logger.info("Initialized the Kafka Client Metrics Reporter Receiver");
    }

    @Override
    public void exportMetrics(AuthorizableRequestContext requestContext, ClientTelemetryPayload telemetryPayload) {
        metricsExporter.export(telemetryPayload);
    }
}

