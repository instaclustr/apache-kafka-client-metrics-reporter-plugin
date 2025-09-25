package com.instaclustr.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.util.Map;

public class KafkaClientMetricsReporterConfig {

    public Map<String, Object> configurations;
    private static final Logger logger = LoggerFactory.getLogger(KafkaClientMetricsReporterConfig.class);

    public KafkaClientMetricsReporterConfig() {
        this(System.getenv("KAFKA_CLIENT_METRICS_CONFIG_PATH"));
    }

    public KafkaClientMetricsReporterConfig(String configFilePath) {
        try {
            Yaml yaml = new Yaml();
            logger.debug("Loading telemetry config from: {}", configFilePath);

            try (FileInputStream fis = new FileInputStream(configFilePath)) {
                this.configurations = yaml.load(fis);
            }

        } catch (final Exception ex) {
            logger.debug("Failed to load telemetry config", ex);
            throw new RuntimeException("Failed to load telemetry config", ex);
        }
    }
}
