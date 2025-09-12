package com.instaclustr.kafka.forwarders;

import com.instaclustr.kafka.KafkaClientMetricsTelemetryPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpMetricsExporter implements MetricsExporter {
    private final String endpoint;
    private final int timeout;
    private final Logger logger = LoggerFactory.getLogger(HttpMetricsExporter.class);

    public HttpMetricsExporter(String endpoint, int timeout) {
        this.endpoint = endpoint;
        this.timeout = timeout;
    }

    @Override
    public void export(KafkaClientMetricsTelemetryPayload payload) {
        logger.info("Forwarding metrics via HTTP to {} with timeout {}: {}", endpoint, timeout, payload);

        HttpURLConnection connection = null;
        try {
            final URL url = new URL(endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            String jsonPayload = payload.toString();
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                logger.warn("Failed to forward metrics. HTTP response code: {}", responseCode);
            }
        } catch (Exception e) {
            logger.error("Error forwarding metrics via HTTP", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
