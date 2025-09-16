package com.instaclustr.kafka.forwarders;

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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class HttpMetricsExporter implements MetricsExporter {
    private final String endpoint;
    private final HttpClient httpClient;
    private final Logger logger = LoggerFactory.getLogger(HttpMetricsExporter.class);

    public HttpMetricsExporter(String endpoint, int timeout) {
        this.endpoint = endpoint;

        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofMillis(timeout))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
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

            MetricsData metricsData = MetricsData.parseFrom(bytes);
            HashMap<String, Object> metadata = new HashMap<>();
            metadata.put("env", "dev");
            metadata.put("region", "us-west");
            metadata.put("randomNumber", 42);
            metadata.put("featureEnabled", true);

            if (!metadata.isEmpty() && metricsData.getResourceMetricsCount() > 0) {
                MetricsData.Builder metricsBuilder = metricsData.toBuilder();
                for (int i = 0; i < metricsBuilder.getResourceMetricsCount(); i++) {
                    Resource.Builder resourceBuilder = metricsBuilder.getResourceMetricsBuilder(i).getResourceBuilder();

                    for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        AnyValue.Builder anyValueBuilder = AnyValue.newBuilder();

                        // Handle common types; extend as needed for your metadata values
                        if (value instanceof String) {
                            anyValueBuilder.setStringValue((String) value);
                        } else if (value instanceof Boolean) {
                            anyValueBuilder.setBoolValue((Boolean) value);
                        } else if (value instanceof Long) {
                            anyValueBuilder.setIntValue((Long) value);
                        } else if (value instanceof Double) {
                            anyValueBuilder.setDoubleValue((Double) value);
                        } else {
                            // Fallback to string for unsupported types
                            anyValueBuilder.setStringValue(value.toString());
                        }

                        KeyValue kv = KeyValue.newBuilder()
                                .setKey(key)
                                .setValue(anyValueBuilder.build())
                                .build();
                        resourceBuilder.addAttributes(kv);
                    }
                }

                // Reserialize the modified MetricsData
                bytes = metricsBuilder.build().toByteArray();
            }



            String payloadAsString = new String(bytes, StandardCharsets.UTF_8);
            logger.info("Original Payload: with enriched {}", payloadAsString);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/x-protobuf")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        logger.info("OTLP metrics endpoint response status code: " + response.statusCode());
                        logger.debug("OTLP metrics endpoint response: {}", response.body());
                        return response;
                    })
                    .thenApply(HttpResponse::body)
                    .exceptionally(ex -> {
                        logger.error("Error invoking the OTLP metrics endpoint", ex);
                        return null;
                    });

        } catch (final Exception e) {
            logger.error("Error exporting OTLP metrics to {}: {}", endpoint, e.getMessage(), e);
        }
    }
}
