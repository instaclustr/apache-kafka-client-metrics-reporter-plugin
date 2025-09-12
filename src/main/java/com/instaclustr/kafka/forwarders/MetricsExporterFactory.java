package com.instaclustr.kafka.forwarders;

import java.util.Map;

public class MetricsExporterFactory {
    public static MetricsExporter create(final Map<String, Object> configurations) {

        Map<String, Object> exporterMap = (Map<String, Object>) configurations.get("exporter");
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
    }
}