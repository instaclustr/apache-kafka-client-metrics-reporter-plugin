package com.instaclustr.kafka.forwarders;

import com.instaclustr.kafka.KafkaClientMetricsTelemetryPayload;

public interface MetricsExporter {
    void forward(KafkaClientMetricsTelemetryPayload payload);
}
