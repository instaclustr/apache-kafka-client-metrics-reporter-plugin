package com.instaclustr.kafka.forwarders;

import com.instaclustr.kafka.KafkaClientMetricsReporterPayload;

public interface MetricsExporter {
    void export(KafkaClientMetricsReporterPayload payload);
}
