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

import java.util.Collections;
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
        final Map<String, Object> metadata = metadataObj != null ? (Map<String, Object>) metadataObj : Collections.emptyMap();

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
