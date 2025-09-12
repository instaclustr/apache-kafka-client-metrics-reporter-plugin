package com.instaclustr.kafka;

public class KafkaClientMetricsReporterPayload {
    public final Object originalPayload;
    public final String metadata;
    public final long timestamp;

    public KafkaClientMetricsReporterPayload(Object originalPayload, String metadata, long timestamp) {
        this.originalPayload = originalPayload;
        this.metadata = metadata;
        this.timestamp = timestamp;
    }
}