package com.instaclustr.kafka.exporters;

import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.apache.kafka.shaded.io.opentelemetry.proto.common.v1.AnyValue;
import org.apache.kafka.shaded.io.opentelemetry.proto.common.v1.KeyValue;
import org.apache.kafka.shaded.io.opentelemetry.proto.metrics.v1.MetricsData;
import org.apache.kafka.shaded.io.opentelemetry.proto.resource.v1.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;

public class HttpMetricsExporter implements MetricsExporter {
    private final String endpoint;
    private final HttpClient httpClient;
    final Map<String, Object> metadata;
    private final Logger logger = LoggerFactory.getLogger(HttpMetricsExporter.class);

    public HttpMetricsExporter(final String endpoint, final int timeoutMillis, final Map<String, Object> metadata) {
        this.endpoint = endpoint;
        this.metadata = metadata;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofMillis(timeoutMillis))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public void export(final ClientTelemetryPayload payload) {
        try {
            final byte[] rawBytes = bufferToBytes(payload.data());
            MetricsData metricsData = MetricsData.parseFrom(rawBytes);

            final byte[] finalBytes = shouldEnrich(metricsData)
                    ? enrichMetricsData(metricsData)
                    : rawBytes;
            HttpRequest request = buildRequest(finalBytes);
            sendAsync(request);
        } catch (Exception e) {
            logger.error("Error exporting OTLP metrics to {}: {}", endpoint, e.getMessage(), e);
        }
    }

    private byte[] bufferToBytes(ByteBuffer buffer) {
        if (buffer.hasArray()) {
            return buffer.array();
        } else {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return bytes;
        }
    }

    private boolean shouldEnrich(final MetricsData metricsData) {
        return !this.metadata.isEmpty() && metricsData.getResourceMetricsCount() > 0;
    }

    private byte[] enrichMetricsData(MetricsData metricsData) {
        MetricsData.Builder dataBuilder = metricsData.toBuilder();

        for (int i = 0; i < dataBuilder.getResourceMetricsCount(); i++) {
            Resource.Builder resourceBuilder =
                    dataBuilder.getResourceMetricsBuilder(i).getResourceBuilder();

            this.metadata.forEach((key, value) ->
                    resourceBuilder.addAttributes(toKeyValue(key, value))
            );
        }

        return dataBuilder.build().toByteArray();
    }

    private KeyValue toKeyValue(final String key, final Object value) {
        return KeyValue.newBuilder()
                .setKey(key)
                .setValue(toAnyValue(value))
                .build();
    }

    private AnyValue toAnyValue(final Object value) {
        AnyValue.Builder b = AnyValue.newBuilder();
        if (value instanceof String) {
            b.setStringValue((String) value);
        } else if (value instanceof Boolean) {
            b.setBoolValue((Boolean) value);
        } else if (value instanceof Long) {
            b.setIntValue((Long) value);
        } else if (value instanceof Integer) {
            b.setIntValue(((Integer) value).longValue());
        } else if (value instanceof Double) {
            b.setDoubleValue((Double) value);
        } else {
            // fallback
            b.setStringValue(value.toString());
        }
        return b.build();
    }

    private HttpRequest buildRequest(final byte[] payload) {
        return HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/x-protobuf")
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                .build();
    }

    private void sendAsync(final HttpRequest request) {
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    logger.info("OTLP metrics endpoint status: {}", response.statusCode());
                    logger.debug("Response body: {}", response.body());
                })
                .exceptionally(ex -> {
                    logger.error("Error invoking the OTLP metrics endpoint", ex);
                    return null;
                });
    }
}