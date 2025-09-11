package com.instaclustr.kafka.forwarders;

import com.instaclustr.kafka.KafkaClientMetricsTelemetryPayload;

public interface MetricsForwarder {
    void forward(KafkaClientMetricsTelemetryPayload payload);
}
