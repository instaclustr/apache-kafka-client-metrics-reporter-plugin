package com.instaclustr.kafka.forwarders;

import com.instaclustr.kafka.KafkaClientMetricsTelemetryPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcMetricsForwarder implements MetricsForwarder {
    private final String endpoint;
    private final int timeout;
    private final Logger logger = LoggerFactory.getLogger(GrpcMetricsForwarder.class);

    public GrpcMetricsForwarder(String endpoint, int timeout) {
        this.endpoint = endpoint;
        this.timeout = timeout;
    }

    @Override
    public void forward(KafkaClientMetricsTelemetryPayload payload) {
        logger.info("Forwarding metrics via gRPC to {} with timeout {}: {}", endpoint, timeout, payload);
    }
}