package com.instaclustr.kafka;

public class KafkaClientMetricsTelemetryPayload {
    public final Object originalPayload;
    public final String metadata;
    public final long timestamp;

    public KafkaClientMetricsTelemetryPayload(Object originalPayload, String metadata, long timestamp) {
        this.originalPayload = originalPayload;
        this.metadata = metadata;
        this.timestamp = timestamp;
    }
}