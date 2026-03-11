/*
Copyright 2021 Instaclustr Pty Ltd

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.instaclustr.kafka;

import com.instaclustr.kafka.logging.KafkaClientMetricsLogger;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.util.Map;

public class KafkaClientMetricsReporterConfig {

    public Map<String, Object> configurations;
    private static final KafkaClientMetricsLogger logger = KafkaClientMetricsLogger.getLogger(KafkaClientMetricsReporterConfig.class);

    public KafkaClientMetricsReporterConfig() {
        this(System.getenv("KAFKA_CLIENT_METRICS_CONFIG_PATH"));
    }

    public KafkaClientMetricsReporterConfig(String configFilePath) {
        try {
            Yaml yaml = new Yaml();
            logger.info("Loading telemetry config from: {}", configFilePath);

            try (FileInputStream fis = new FileInputStream(configFilePath)) {
                this.configurations = yaml.load(fis);
            }

        } catch (final Exception ex) {
            logger.debug("Failed to load telemetry config", ex);
            throw new RuntimeException("Failed to load telemetry config", ex);
        }
    }
}
