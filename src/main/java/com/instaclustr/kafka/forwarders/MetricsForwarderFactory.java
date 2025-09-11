package com.instaclustr.kafka.forwarders;

import com.instaclustr.kafka.KafkaClientMetricsTelemetryConfig;

public class MetricsForwarderFactory {
    public static MetricsForwarder create(KafkaClientMetricsTelemetryConfig config) {
        switch (config.forwarder.mode) {
            case HTTP:
                return new HttpMetricsForwarder(config.forwarder.endpoint, config.forwarder.timeout);
            case GRPC:
                return new GrpcMetricsForwarder(config.forwarder.endpoint, config.forwarder.timeout);
            case LOG:
                return new LogMetricsForwarder(config.forwarder.logPath);
            default:
                throw new IllegalArgumentException("Unknown mode: " + config.forwarder.mode);
        }
    }
}
