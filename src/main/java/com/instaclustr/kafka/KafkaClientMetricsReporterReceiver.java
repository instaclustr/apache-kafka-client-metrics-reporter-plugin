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

import com.instaclustr.kafka.exporters.MetricsExporter;
import com.instaclustr.kafka.exporters.MetricsExporterFactory;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.apache.kafka.server.telemetry.ClientTelemetryReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaClientMetricsReporterReceiver implements ClientTelemetryReceiver {

    private final MetricsExporter metricsExporter;
    private static final Logger logger = LoggerFactory.getLogger(KafkaClientMetricsReporterReceiver.class);

    public KafkaClientMetricsReporterReceiver() {
        logger.info("Initializing the Kafka Client Metrics Reporter Receiver");
        final KafkaClientMetricsReporterConfig kafkaClientMetricsReporterConfig = new KafkaClientMetricsReporterConfig();
        this.metricsExporter = MetricsExporterFactory.create(kafkaClientMetricsReporterConfig.configurations);
        logger.info("Initialized the Kafka Client Metrics Reporter Receiver");
    }

    @Override
    public void exportMetrics(final AuthorizableRequestContext requestContext, final ClientTelemetryPayload telemetryPayload) {
        metricsExporter.export(requestContext, telemetryPayload);
    }
}

