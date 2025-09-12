package com.instaclustr.kafka;

import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class KafkaClientMetricsTelemetryConfig {
    public Exporter exporter;
    public Map<String, Object> metadata;

    public static class Exporter {
        public enum Mode { HTTP, GRPC, LOG }
        public Mode mode;
        public int timeout;
        public String endpoint;
        public String logPath;
    }

    public static KafkaClientMetricsTelemetryConfig load(final String path) {
        Yaml yaml = new Yaml();
        try (FileInputStream fis = new FileInputStream(path)) {
            Map<String, Object> obj = yaml.load(fis);
            KafkaClientMetricsTelemetryConfig config = new KafkaClientMetricsTelemetryConfig();

            Map<String, Object> forwarderMap = (Map<String, Object>) obj.get("forwarder");
            config.exporter = new Exporter();
            config.exporter.mode = Exporter.Mode.valueOf(((String) forwarderMap.get("mode")).toUpperCase());
            config.exporter.timeout = (int) forwarderMap.get("timeout");
            config.exporter.logPath = (String) forwarderMap.get("logPath");
            config.exporter.endpoint = (String) forwarderMap.get("endpoint");

            config.metadata = (Map<String, Object>) obj.get("metadata");

            return config;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load telemetry config", e);
        }
    }
}