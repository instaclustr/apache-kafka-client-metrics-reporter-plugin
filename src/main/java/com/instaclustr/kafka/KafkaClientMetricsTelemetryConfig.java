package com.instaclustr.kafka;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class KafkaClientMetricsTelemetryConfig {
    public enum Mode { HTTP, GRPC, LOG }
    public Mode mode;
    public String endpoint;
    public String logPath;
    public String metadata;

    public static KafkaClientMetricsTelemetryConfig load(final String path) {
        Properties props = new Properties();
        KafkaClientMetricsTelemetryConfig config = new KafkaClientMetricsTelemetryConfig();
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
            config.mode = Mode.valueOf(props.getProperty("mode", "LOG"));
            config.endpoint = props.getProperty("endpoint", "");
            config.logPath = props.getProperty("logPath", "metrics.log");
            config.metadata = props.getProperty("metadata", "unknown");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load telemetry config", e);
        }
        return config;
    }
}