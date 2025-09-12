package com.instaclustr.kafka;

import com.instaclustr.kafka.forwarders.MetricsExporter;
import com.instaclustr.kafka.forwarders.MetricsExporterFactory;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.Map;

public class KafkaClientMetricsTelemetryReceiver implements ClientTelemetryReceiver {

    private final MetricsExporter metricsExporter;
    public Map<String, Object> metadata;

    public KafkaClientMetricsTelemetryReceiver() {
        this.metricsExporter = MetricsExporterFactory.create();
        this.metadata = loadMetadataFromYamlConfig();
    }

    @Override
    public void exportMetrics(AuthorizableRequestContext requestContext, ClientTelemetryPayload telemetryPayload) {
        KafkaClientMetricsTelemetryPayload enrichedPayload = new KafkaClientMetricsTelemetryPayload(
                telemetryPayload,
                metadata.toString(),
                System.currentTimeMillis()
        );
        metricsExporter.forward(enrichedPayload);
    }

    public Map<String, Object> loadMetadataFromYamlConfig() {
        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        String configFilePath = System.getenv("KAFKA_CLIENT_METRICS_CONFIG_PATH");
        if (configFilePath == null) {
            throw new IllegalStateException("Environment variable KAFKA_CLIENT_METRICS_CONFIG_PATH is not set.");
        }
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            Map<String, Object> obj = yaml.load(fis);
            Object metadataObj = obj.get("metadata");
            if (metadataObj instanceof Map) {
                return (Map<String, Object>) metadataObj;
            } else {
                throw new IllegalStateException("Metadata section missing or not a map in YAML config.");
            }
        } catch (final Exception e) {
            return Collections.emptyMap();
        }
    }
}

