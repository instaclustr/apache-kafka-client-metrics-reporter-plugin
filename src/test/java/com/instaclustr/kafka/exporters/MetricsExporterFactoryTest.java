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
