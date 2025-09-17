package com.instaclustr.kafka.exporters;

import com.instaclustr.kafka.helpers.MetricsMetaDataProcessor;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
            final MetricsMetaDataProcessor processor = new MetricsMetaDataProcessor(metadata);
            final byte[] finalBytes = processor.processMetricsData(payload.data());

            HttpRequest request = buildRequest(finalBytes);
            sendAsync(request);
        } catch (Exception e) {
            logger.error("Error exporting OTLP metrics to {}: {}", endpoint, e.getMessage(), e);
        }
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