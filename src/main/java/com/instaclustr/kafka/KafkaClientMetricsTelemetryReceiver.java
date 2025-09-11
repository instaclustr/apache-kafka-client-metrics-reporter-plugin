package com.instaclustr.kafka;

import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaClientMetricsTelemetryReceiver implements ClientTelemetryReceiver {

    private static final Logger logger = LoggerFactory.getLogger(KafkaClientMetricsTelemetryReceiver.class);
    private final KafkaClientMetricsTelemetryConfig kafkaClientMetricsTelemetryConfig;

    public KafkaClientMetricsTelemetryReceiver(KafkaClientMetricsTelemetryConfig kafkaClientMetricsTelemetryConfig) {
        this.kafkaClientMetricsTelemetryConfig = kafkaClientMetricsTelemetryConfig;
    }

    @Override
    public void exportMetrics(AuthorizableRequestContext requestContext, ClientTelemetryPayload telemetryPayload) {
        KafkaClientMetricsTelemetryPayload enrichedPayload = new KafkaClientMetricsTelemetryPayload(
                telemetryPayload,
                kafkaClientMetricsTelemetryConfig.metadata,
                System.currentTimeMillis()
        );

        switch (kafkaClientMetricsTelemetryConfig.mode) {
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
        logger.info("Forwarding metrics via HTTP to {}: {}", kafkaClientMetricsTelemetryConfig.endpoint, payload);
    }

    private void forwardViaGrpc(KafkaClientMetricsTelemetryPayload payload) {
        logger.info("Forwarding metrics via gRPC to {}: {}", kafkaClientMetricsTelemetryConfig.endpoint, payload);
    }

    private void logPayload(KafkaClientMetricsTelemetryPayload payload) {
        logger.info("Logging metrics to {}: {}", kafkaClientMetricsTelemetryConfig.logPath, payload);
    }
}