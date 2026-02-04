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

package com.instaclustr.kafka.helpers;

import com.instaclustr.kafka.logging.KafkaClientMetricsLogger;
import org.apache.kafka.common.compress.Compression;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.requests.RequestContext;
import org.apache.kafka.common.utils.BufferSupplier;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.shaded.com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.shaded.io.opentelemetry.proto.common.v1.AnyValue;
import org.apache.kafka.shaded.io.opentelemetry.proto.common.v1.KeyValue;
import org.apache.kafka.shaded.io.opentelemetry.proto.metrics.v1.MetricsData;
import org.apache.kafka.shaded.io.opentelemetry.proto.resource.v1.Resource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MetricsMetaDataProcessor {
    private static final KafkaClientMetricsLogger logger = KafkaClientMetricsLogger.getLogger(MetricsMetaDataProcessor.class);
    private final Map<String, Object> metadata;


    public MetricsMetaDataProcessor(final Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public byte[] processMetricsData(final AuthorizableRequestContext requestContext, final ByteBuffer buffer) {
        final int bufferRemaining = buffer.remaining();
        byte[] rawBytes = null;
        try {
            rawBytes = bufferToBytes(buffer);
            final MetricsData metricsData = decodeMetricsData(rawBytes);
            if (metricsData == null) {
                return rawBytes;
            }
            return enrichMetricsData(requestContext, metricsData);
        } catch (Exception e) {
            logger.error("Error processing metrics data (bufferRemaining={}, rawBytesLength={}): {}",
                    bufferRemaining, rawBytes != null ? rawBytes.length : "null", e.getMessage(), e);
        }

        return rawBytes != null ? rawBytes : bufferToBytes(buffer);
    }

    private MetricsData decodeMetricsData(final byte[] rawBytes) {
        if (rawBytes == null || rawBytes.length == 0) {
            return null;
        }

        // Fast path: payload is uncompressed OTLP protobuf.
        try {
            return MetricsData.parseFrom(rawBytes);
        } catch (final InvalidProtocolBufferException ignored) {
        }

        // Slow path: try to decompress with common Kafka compression types.
        for (final CompressionType compressionType : Arrays.asList(
                CompressionType.ZSTD,
                CompressionType.GZIP,
                CompressionType.LZ4,
                CompressionType.SNAPPY
        )) {
            try {
                final byte[] decompressed = decompress(rawBytes, compressionType);
                final MetricsData parsed = MetricsData.parseFrom(decompressed);
                logger.debug("Decoded client telemetry payload using compression={}", compressionType.name);
                return parsed;
            } catch (final Exception ignored) {
            }
        }

        logger.error("Unable to decode client telemetry payload as raw protobuf or with common compression codecs");
        return null;
    }

    private byte[] decompress(final byte[] data, final CompressionType compressionType) throws Exception {
        if (compressionType == CompressionType.NONE) {
            return data;
        }

        final ByteBuffer buffer = ByteBuffer.wrap(data);
        final Compression compression = Compression.of(compressionType).build();

        try (InputStream inputStream = compression.wrapForInput(buffer, (byte) 0, BufferSupplier.NO_CACHING);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            inputStream.transferTo(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] bufferToBytes(ByteBuffer buffer) {
        if (buffer.hasArray() && buffer.arrayOffset() == 0 && buffer.remaining() == buffer.array().length) {
            return buffer.array();
        } else {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.duplicate().get(bytes);
            return bytes;
        }
    }

    private boolean shouldEnrichStaticMetaData(final MetricsData metricsData) {
        return !this.metadata.isEmpty() && metricsData.getResourceMetricsCount() > 0;
    }

    private byte[] enrichMetricsData(final AuthorizableRequestContext context, MetricsData metricsData) {
        MetricsData.Builder dataBuilder = metricsData.toBuilder();

        if(shouldEnrichStaticMetaData(metricsData)){
            enrichStaticMetadata(dataBuilder);
        }
        enrichDynamicMetadata(context, dataBuilder);

        return dataBuilder.build().toByteArray();
    }

    private void enrichStaticMetadata(MetricsData.Builder dataBuilder) {
        for (int i = 0; i < dataBuilder.getResourceMetricsCount(); i++) {
            Resource.Builder resourceBuilder =
                    dataBuilder.getResourceMetricsBuilder(i).getResourceBuilder();

            this.metadata.forEach((key, value) ->
                    resourceBuilder.addAttributes(toKeyValue(key, value))
            );
        }
    }

    private void enrichDynamicMetadata(final AuthorizableRequestContext context, MetricsData.Builder dataBuilder) {

        final RequestContext requestContext = (RequestContext) context;

        Map<String, String> dynamicMetadata = new HashMap<>();
        if (requestContext.clientId() != null) {
            dynamicMetadata.put("clientId", requestContext.clientId());
        }
        if (requestContext.clientInformation != null) {
            dynamicMetadata.put("clientSoftwareName", requestContext.clientInformation.softwareName());
            dynamicMetadata.put("clientSoftwareVersion", requestContext.clientInformation.softwareVersion());
        }

        for (int i = 0; i < dataBuilder.getResourceMetricsCount(); i++) {
            Resource.Builder resourceBuilder = dataBuilder.getResourceMetricsBuilder(i).getResourceBuilder();
            dynamicMetadata.forEach((key, value) -> resourceBuilder.addAttributes(
                    KeyValue.newBuilder()
                            .setKey(key)
                            .setValue(AnyValue.newBuilder().setStringValue(value))
                            .build()
            ));
        }
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