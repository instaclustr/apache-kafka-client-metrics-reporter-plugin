package com.instaclustr.kafka.forwarders;

import org.apache.kafka.server.telemetry.ClientTelemetryPayload;

public interface MetricsExporter {
    void export(ClientTelemetryPayload payload);
}
