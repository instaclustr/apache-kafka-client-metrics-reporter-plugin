package com.instaclustr.kafka.exporters;

import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;

public interface MetricsExporter {
    void export(final AuthorizableRequestContext requestContext, final ClientTelemetryPayload payload);
}
