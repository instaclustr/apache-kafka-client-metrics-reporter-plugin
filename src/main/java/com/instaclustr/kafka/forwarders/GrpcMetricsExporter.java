package com.instaclustr.kafka.forwarders;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class GrpcMetricsExporter implements MetricsExporter {
    private final String endpoint;
    private final int timeout;
    private final ManagedChannel channel;
    private final MetricsServiceGrpc.MetricsServiceBlockingStub stub;
    private final Logger logger = LoggerFactory.getLogger(GrpcMetricsExporter.class);

    public GrpcMetricsExporter(String endpoint, int timeout) {
        this.endpoint   = endpoint;
        this.timeout = timeout;
        this.channel = ManagedChannelBuilder
                .forTarget(endpoint)
                .usePlaintext()
                .build();
        this.stub = MetricsServiceGrpc
                .newBlockingStub(channel)
                .withDeadlineAfter(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void export(ClientTelemetryPayload payload) {
        try {
            ByteBuffer byteBuffer = payload.data();
            byte[] bytes;

            if (byteBuffer.hasArray()) {
                bytes = byteBuffer.array();
            } else {
                bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
            }

            ExportMetricsServiceRequest req = ExportMetricsServiceRequest.parseFrom(bytes);

            stub.export(req);
            logger.info("Successfully exported {} ResourceMetrics to {}",
                    req.getResourceMetricsCount(), endpoint);
        } catch (Exception e) {
            logger.error("Failed to export OTLP metrics to {}: {}", endpoint, e.getMessage(), e);
        }
    }

    /**
     * Call this on shutdown to cleanly close the gRPC channel.
     */
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(this.timeout, TimeUnit.MILLISECONDS);
    }
}