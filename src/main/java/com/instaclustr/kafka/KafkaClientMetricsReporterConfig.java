package com.instaclustr.kafka;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class KafkaClientMetricsReporterConfig {

    public Map<String, Object> configurations;

    public KafkaClientMetricsReporterConfig() {
        try {
            Yaml yaml = new Yaml();
            final String kafkaClientMetricsConfigFilePath = System.getenv("KAFKA_CLIENT_METRICS_CONFIG_PATH");
            final FileInputStream fis = new FileInputStream(kafkaClientMetricsConfigFilePath);
            this.configurations = yaml.load(fis);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load telemetry config", e);
        }
    }
}