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

package com.instaclustr.kafka.exporters;

import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

public class MetricsExporterFactoryTest {

    @Test
    public void testCreateHttpExporter() {
        Map<String, Object> exporterConfig = new HashMap<>();
        exporterConfig.put("mode", "HTTP");
        exporterConfig.put("endpoint", "http://localhost:8080/metrics");
        exporterConfig.put("timeout", 1000);

        Map<String, Object> metadata = Collections.singletonMap("key", "value");

        Map<String, Object> config = new HashMap<>();
        config.put("exporter", exporterConfig);
        config.put("metadata", metadata);

        MetricsExporter exporter = MetricsExporterFactory.create(config);
        assertTrue(exporter instanceof HttpMetricsExporter);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidExporterConfigThrows() {
        Map<String, Object> config = new HashMap<>();
        config.put("exporter", "notAMap");
        MetricsExporterFactory.create(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidMetadataConfigThrows() {
        Map<String, Object> exporterConfig = new HashMap<>();
        exporterConfig.put("mode", "HTTP");
        exporterConfig.put("endpoint", "http://localhost:8080/metrics");
        exporterConfig.put("timeout", 1000);

        Map<String, Object> config = new HashMap<>();
        config.put("exporter", exporterConfig);
        config.put("metadata", "notAMap");

        MetricsExporterFactory.create(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnknownModeThrows() {
        Map<String, Object> exporterConfig = new HashMap<>();
        exporterConfig.put("mode", "UNKNOWN");
        exporterConfig.put("endpoint", "http://localhost:8080/metrics");
        exporterConfig.put("timeout", 1000);

        Map<String, Object> config = new HashMap<>();
        config.put("exporter", exporterConfig);

        MetricsExporterFactory.create(config);
    }
}
