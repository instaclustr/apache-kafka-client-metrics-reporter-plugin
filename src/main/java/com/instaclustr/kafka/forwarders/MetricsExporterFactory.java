package com.instaclustr.kafka.forwarders;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;


public class MetricsExporterFactory {
    public static MetricsExporter create() {
        Yaml yaml = new Yaml();
        try {
            final String kafkaClientMetricsConfigFilePath = System.getenv("KAFKA_CLIENT_METRICS_CONFIG_PATH");
            final FileInputStream fis = new FileInputStream(kafkaClientMetricsConfigFilePath);
            Map<String, Object> obj = yaml.load(fis);

            Map<String, Object> exporterMap = (Map<String, Object>) obj.get("exporter");
            switch ((((String) exporterMap.get("mode")).toUpperCase())) {
                case "HTTP":
                    return new HttpMetricsExporter((String) exporterMap.get("endpoint"), (int) exporterMap.get("timeout"));
                case "GRPC":
                    return new GrpcMetricsExporter((String) exporterMap.get("endpoint"), (int) exporterMap.get("timeout"));
                case "LOG":
                    return new LogMetricsExporter((String) exporterMap.get("logPath"));
                default:
                    throw new IllegalArgumentException("Unknown mode: ");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load telemetry config", e);
        }
    }


}



//
//package com.instaclustr.kafka;
//
//import org.yaml.snakeyaml.Yaml;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.Map;
//
//public class KafkaClientMetricsTelemetryConfig {
//    public com.instaclustr.kafka.KafkaClientMetricsTelemetryConfig.Exporter exporter;
//    public Map<String, Object> metadata;
//
//    public static class Exporter {
//        public enum Mode { HTTP, GRPC, LOG }
//        public com.instaclustr.kafka.KafkaClientMetricsTelemetryConfig.Exporter.Mode mode;
//        public int timeout;
//        public String endpoint;
//        public String logPath;
//    }
//
//    public static com.instaclustr.kafka.KafkaClientMetricsTelemetryConfig loadKafkaClientMetricsTelemetryConfig(final String path) {
//        Yaml yaml = new Yaml();
//        try (FileInputStream fis = new FileInputStream(path)) {
//            Map<String, Object> obj = yaml.load(fis);
//            com.instaclustr.kafka.KafkaClientMetricsTelemetryConfig config = new com.instaclustr.kafka.KafkaClientMetricsTelemetryConfig();
//
//            Map<String, Object> exporterMap = (Map<String, Object>) obj.get("exporter");
//            config.exporter = new com.instaclustr.kafka.KafkaClientMetricsTelemetryConfig.Exporter();
//            config.exporter.mode = com.instaclustr.kafka.KafkaClientMetricsTelemetryConfig.Exporter.Mode.valueOf(((String) exporterMap.get("mode")).toUpperCase());
//            config.exporter.timeout = (int) exporterMap.get("timeout");
//            config.exporter.logPath = (String) exporterMap.get("logPath");
//            config.exporter.endpoint = (String) exporterMap.get("endpoint");
//
//            config.metadata = (Map<String, Object>) obj.get("metadata");
//
//            return config;
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to load telemetry config", e);
//        }
//    }
//}
