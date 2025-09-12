package com.instaclustr.kafka;

import com.instaclustr.kafka.forwarders.MetricsExporter;
import com.instaclustr.kafka.forwarders.MetricsExporterFactory;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;

import java.util.Map;

public class KafkaClientMetricsReporterReceiver implements ClientTelemetryReceiver {

    private final MetricsExporter metricsExporter;
    private final Map<String, Object> metadata;

    public KafkaClientMetricsReporterReceiver() {
        final KafkaClientMetricsReporterConfig kafkaClientMetricsReporterConfig = new KafkaClientMetricsReporterConfig();
        this.metricsExporter = MetricsExporterFactory.create(kafkaClientMetricsReporterConfig.configurations);
        this.metadata = (Map<String, Object>) kafkaClientMetricsReporterConfig.configurations.get("metadata");
    }

    @Override
    public void exportMetrics(AuthorizableRequestContext requestContext, ClientTelemetryPayload telemetryPayload) {
        KafkaClientMetricsReporterPayload enrichedPayload = new KafkaClientMetricsReporterPayload(
                telemetryPayload,
                metadata.toString(),
                System.currentTimeMillis()
        );
        metricsExporter.export(enrichedPayload);
    }
}

