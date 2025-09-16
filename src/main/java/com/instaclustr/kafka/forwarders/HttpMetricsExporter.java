package com.instaclustr.kafka.forwarders;

import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

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

            String payloadAsString = new String(bytes, StandardCharsets.UTF_8);
            logger.info("Original Payload: {}", payloadAsString);

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
