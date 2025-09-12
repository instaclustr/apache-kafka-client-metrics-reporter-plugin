package com.instaclustr.kafka;

import com.instaclustr.kafka.forwarders.MetricsExporter;
import com.instaclustr.kafka.forwarders.MetricsExporterFactory;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;

public class KafkaClientMetricsTelemetryReceiver implements ClientTelemetryReceiver {

    private final KafkaClientMetricsTelemetryConfig kafkaClientMetricsTelemetryConfig;
    private final MetricsExporter metricsExporter;

    public KafkaClientMetricsTelemetryReceiver(KafkaClientMetricsTelemetryConfig kafkaClientMetricsTelemetryConfig) {
        this.kafkaClientMetricsTelemetryConfig = kafkaClientMetricsTelemetryConfig;
        this.metricsExporter = MetricsExporterFactory.create(kafkaClientMetricsTelemetryConfig);

    }

    @Override
    public void exportMetrics(AuthorizableRequestContext requestContext, ClientTelemetryPayload telemetryPayload) {
        KafkaClientMetricsTelemetryPayload enrichedPayload = new KafkaClientMetricsTelemetryPayload(
                telemetryPayload,
                kafkaClientMetricsTelemetryConfig.metadata.toString(),
                System.currentTimeMillis()
        );
        metricsExporter.forward(enrichedPayload);
    }
}