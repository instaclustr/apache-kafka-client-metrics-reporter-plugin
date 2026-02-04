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

import java.util.List;
import java.util.Map;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.apache.kafka.common.metrics.MetricsReporter;
import org.apache.kafka.server.telemetry.ClientTelemetry;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;
import com.instaclustr.kafka.logging.KafkaClientMetricsLogger;

public class KafkaClientMetricsReporter implements MetricsReporter, ClientTelemetry {

    private static final KafkaClientMetricsLogger logger = KafkaClientMetricsLogger.getLogger(KafkaClientMetricsReporter.class);


    @Override
    public void init(List<KafkaMetric> metrics) {
        logger.info("Initializing the client metric reporter: {}", metrics);
    }

    @Override
    public void configure(final Map<String, ?> configs) {
        logger.info("Configuration of the reporter");
    }

    @Override
    public void metricChange(final KafkaMetric metric) {
        logger.debug("Changing the metric {}", metric.metricName());
    }

    @Override
    public void metricRemoval(final KafkaMetric metric) {
        logger.debug("Removing the metric {}", metric.metricName());
    }

    @Override
    public void close() {
        logger.debug("Closing the reporter");
    }

    @Override
    public ClientTelemetryReceiver clientReceiver() {
        return new KafkaClientMetricsReporterReceiver();
    }
}