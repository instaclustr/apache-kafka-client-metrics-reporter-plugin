package com.instaclustr.kafka;

import com.instaclustr.kafka.forwarders.MetricsForwarder;
import com.instaclustr.kafka.forwarders.MetricsForwarderFactory;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaClientMetricsTelemetryReceiver implements ClientTelemetryReceiver {

    private static final Logger logger = LoggerFactory.getLogger(KafkaClientMetricsTelemetryReceiver.class);
    private final KafkaClientMetricsTelemetryConfig kafkaClientMetricsTelemetryConfig;
    private final MetricsForwarder metricsForwarder;

    public KafkaClientMetricsTelemetryReceiver(KafkaClientMetricsTelemetryConfig kafkaClientMetricsTelemetryConfig) {
        this.kafkaClientMetricsTelemetryConfig = kafkaClientMetricsTelemetryConfig;
        this.metricsForwarder = MetricsForwarderFactory.create(kafkaClientMetricsTelemetryConfig);

    }

    @Override
    public void exportMetrics(AuthorizableRequestContext requestContext, ClientTelemetryPayload telemetryPayload) {
        KafkaClientMetricsTelemetryPayload enrichedPayload = new KafkaClientMetricsTelemetryPayload(
                telemetryPayload,
                kafkaClientMetricsTelemetryConfig.metadata.toString(),
                System.currentTimeMillis()
        );
        metricsForwarder.forward(enrichedPayload);
    }
}