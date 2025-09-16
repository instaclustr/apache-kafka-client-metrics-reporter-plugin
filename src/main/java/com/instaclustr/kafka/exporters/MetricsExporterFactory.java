package com.instaclustr.kafka.exporters;

import java.util.Map;

public class MetricsExporterFactory {

    private MetricsExporterFactory() {}

    @SuppressWarnings("unchecked")
    public static MetricsExporter create(final Map<String, Object> configurations) {

        final Object exporterObj = configurations.get("exporter");
        if (!(exporterObj instanceof Map)) {
            throw new IllegalArgumentException("Exporter configuration is not a valid map");
        }
        final Map<String, Object> exporterMap = (Map<String, Object>) exporterObj;

        Object metadataObj = configurations.get("metadata");
        if (metadataObj != null && !(metadataObj instanceof Map)) {
            throw new IllegalArgumentException("Metadata configuration is not a valid map");
        }
        final Map<String, Object> metadata = (Map<String, Object>) metadataObj;

        switch (((String) exporterMap.get("mode")).toUpperCase()) {
            case "HTTP":
                return new HttpMetricsExporter(
                        (String) exporterMap.get("endpoint"),
                        (int) exporterMap.get("timeout"),
                        metadata
                );

            default:
                throw new IllegalArgumentException("Unknown mode: " + exporterMap.get("mode"));
        }
    }
}
