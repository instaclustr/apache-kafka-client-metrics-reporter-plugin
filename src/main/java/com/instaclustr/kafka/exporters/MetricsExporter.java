package com.instaclustr.kafka.exporters;

import org.apache.kafka.server.telemetry.ClientTelemetryPayload;

public interface MetricsExporter {
    void export(ClientTelemetryPayload payload);
}
