package com.instaclustr.kafka;

import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaClientMetricsTelemetryReceiver implements ClientTelemetryReceiver {

    private static final Logger logger = LoggerFactory.getLogger(KafkaClientMetricsTelemetryReceiver.class);
    private final KafkaClientMetricsTelemetryConfig config;

    public KafkaClientMetricsTelemetryReceiver(KafkaClientMetricsTelemetryConfig config) {
        this.config = config;
    }

    @Override
    public void exportMetrics(AuthorizableRequestContext requestContext, ClientTelemetryPayload telemetryPayload) {
        // Enrich payload with clusterId, nodeId, timestamp
        KafkaClientMetricsTelemetryPayload enrichedPayload = new KafkaClientMetricsTelemetryPayload(
                telemetryPayload,
                config.metadata,
                System.currentTimeMillis()
        );

        switch (config.mode) {
            case HTTP:
                forwardViaHttp(enrichedPayload);
                break;
            case GRPC:
                forwardViaGrpc(enrichedPayload);
                break;
            case LOG:
                logPayload(enrichedPayload);
                break;
        }
    }

    private void forwardViaHttp(KafkaClientMetricsTelemetryPayload payload) {
        logger.info("Forwarding metrics via HTTP to {}: {}", config.endpoint, payload);
    }

    private void forwardViaGrpc(KafkaClientMetricsTelemetryPayload payload) {
        logger.info("Forwarding metrics via gRPC to {}: {}", config.endpoint, payload);
    }

    private void logPayload(KafkaClientMetricsTelemetryPayload payload) {
        logger.info("Logging metrics to {}: {}", config.logPath, payload);
    }
}