package com.instaclustr.kafka.forwarders;

import com.instaclustr.kafka.KafkaClientMetricsTelemetryConfig;

public class MetricsExporterFactory {
    public static MetricsExporter create(final KafkaClientMetricsTelemetryConfig kafkaClientMetricsTelemetryConfig) {
        switch (kafkaClientMetricsTelemetryConfig.exporter.mode) {
            case HTTP:
                return new HttpMetricsExporter(kafkaClientMetricsTelemetryConfig.exporter.endpoint, kafkaClientMetricsTelemetryConfig.exporter.timeout);
            case GRPC:
                return new GrpcMetricsExporter(kafkaClientMetricsTelemetryConfig.exporter.endpoint, kafkaClientMetricsTelemetryConfig.exporter.timeout);
            case LOG:
                return new LogMetricsExporter(kafkaClientMetricsTelemetryConfig.exporter.logPath);
            default:
                throw new IllegalArgumentException("Unknown mode: " + kafkaClientMetricsTelemetryConfig.exporter.mode);
        }
    }
}
