package com.instaclustr.kafka.helpers;

import io.opentelemetry.proto.metrics.v1.MetricsData;
import io.opentelemetry.proto.resource.v1.Resource;
import org.apache.kafka.common.requests.RequestContext;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;

public class MetricsMetaDataProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MetricsMetaDataProcessor.class);
    private final Map<String, Object> metadata;

    public MetricsMetaDataProcessor(final Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public byte[] processMetricsData(final AuthorizableRequestContext requestContext, final ByteBuffer buffer) {
        final byte[] rawBytes = bufferToBytes(buffer);

        try {
            MetricsData metricsData = MetricsData.parseFrom(rawBytes);
            return enrichMetricsData(requestContext, metricsData);
        } catch (Exception e) {
            logger.error("Error processing metrics data: {}", e.getMessage(), e);
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