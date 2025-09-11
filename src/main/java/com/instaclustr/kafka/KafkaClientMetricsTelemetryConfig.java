package com.instaclustr.kafka;

import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class KafkaClientMetricsTelemetryConfig {
    public Forwarder forwarder;
    public Map<String, Object> metadata;

    public static class Forwarder {
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
            config.forwarder = new Forwarder();
            config.forwarder.mode = Forwarder.Mode.valueOf(((String) forwarderMap.get("mode")).toUpperCase());
            config.forwarder.timeout = (int) forwarderMap.get("timeout");

            config.metadata = (Map<String, Object>) obj.get("metdata");

            return config;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load telemetry config", e);
        }
    }
}