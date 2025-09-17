package com.instaclustr.kafka.helpers;

import io.opentelemetry.proto.metrics.v1.MetricsData;
import io.opentelemetry.proto.resource.v1.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Map;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;

public class MetricsMetaDataProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MetricsMetaDataProcessor.class);
    private final Map<String, Object> metadata;

    public MetricsMetaDataProcessor(final Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public byte[] processMetricsData(final ByteBuffer buffer) {
        final byte[] rawBytes = bufferToBytes(buffer);

        if (shouldEnrich(rawBytes)) {
            try {
                MetricsData metricsData = MetricsData.parseFrom(rawBytes);
                return enrichMetricsData(metricsData);
            } catch (Exception e) {
                logger.error("Error enriching metrics data: {}", e.getMessage(), e);
            }
        }

        return rawBytes;
    }

    private byte[] bufferToBytes(ByteBuffer buffer) {
        if (buffer.hasArray()) {
            return buffer.array();
        } else {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return bytes;
        }
    }

    private boolean shouldEnrich(final byte[] rawBytes) {
        try {
            final MetricsData metricsData = MetricsData.parseFrom(rawBytes);
            return !this.metadata.isEmpty() && metricsData.getResourceMetricsCount() > 0;
        } catch (final Exception ex) {
            logger.error("Error checking if should enrich metrics data: {}", ex.getMessage(), ex);
            return false;
        }
    }

    private byte[] enrichMetricsData(MetricsData metricsData) {
        MetricsData.Builder dataBuilder = metricsData.toBuilder();

        for (int i = 0; i < dataBuilder.getResourceMetricsCount(); i++) {
            Resource.Builder resourceBuilder =
                    dataBuilder.getResourceMetricsBuilder(i).getResourceBuilder();

            this.metadata.forEach((key, value) ->
                    resourceBuilder.addAttributes(toKeyValue(key, value))
            );
        }

        return dataBuilder.build().toByteArray();
    }

    private KeyValue toKeyValue(final String key, final Object value) {
        return KeyValue.newBuilder()
                .setKey(key)
                .setValue(toAnyValue(value))
                .build();
    }

    private AnyValue toAnyValue(final Object value) {
        AnyValue.Builder b = AnyValue.newBuilder();
        if (value instanceof String) {
            b.setStringValue((String) value);
        } else if (value instanceof Boolean) {
            b.setBoolValue((Boolean) value);
        } else if (value instanceof Long) {
            b.setIntValue((Long) value);
        } else if (value instanceof Integer) {
            b.setIntValue(((Integer) value).longValue());
        } else if (value instanceof Double) {
            b.setDoubleValue((Double) value);
        } else {
            b.setStringValue(value.toString());
        }
        return b.build();
    }
}