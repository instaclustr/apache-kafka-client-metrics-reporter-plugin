package com.instaclustr.kafka.helpers;

import io.opentelemetry.proto.metrics.v1.MetricsData;
import io.opentelemetry.proto.resource.v1.Resource;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import org.apache.kafka.common.network.ClientInformation;
import org.apache.kafka.common.network.ListenerName;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.requests.RequestContext;
import org.apache.kafka.common.requests.RequestHeader;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MetricsMetaDataProcessorTest {


    private MetricsData createMetricsDataWithNoResource() {
        Resource resource = Resource.newBuilder().build();
        ResourceMetrics resourceMetrics = ResourceMetrics.newBuilder()
                .setResource(resource)
                .build();
        return MetricsData.newBuilder()
                .addResourceMetrics(resourceMetrics)
                .build();
    }

    private MetricsData createMetricsDataWithResource() {
        Resource resource = Resource.newBuilder()
                .addAttributes(io.opentelemetry.proto.common.v1.KeyValue.newBuilder()
                        .setKey("dummy")
                        .setValue(io.opentelemetry.proto.common.v1.AnyValue.newBuilder().setStringValue("dummyValue").build())
                        .build())
                .build();
        ResourceMetrics resourceMetrics = ResourceMetrics.newBuilder()
                .setResource(resource)
                .build();
        return MetricsData.newBuilder()
                .addResourceMetrics(resourceMetrics)
                .build();
    }

    private ByteBuffer toByteBuffer(MetricsData data) {
        return ByteBuffer.wrap(data.toByteArray());
    }

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessMetricsDataNoEnrichmentDueToEmptyMetadata() {
        MetricsData metricsData = createMetricsDataWithNoResource();
        ByteBuffer buffer = toByteBuffer(metricsData);
        MetricsMetaDataProcessor processor = new MetricsMetaDataProcessor(Collections.emptyMap());
        RequestContext context = getRequestContext();
        byte[] resultBytes = processor.processMetricsData(context, buffer);
        Assert.assertEquals(resultBytes, metricsData.toByteArray());
    }

    @Test
    public void testProcessMetricsDataEnrichData() throws Exception {
        // Create Mock MetricsData with Static and Dynamic metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("staticKey", "staticValue");
        MetricsData metricsData = createMetricsDataWithResource();
        ByteBuffer buffer = toByteBuffer(metricsData);

        // Process and Enrich
        MetricsMetaDataProcessor processor = new MetricsMetaDataProcessor(metadata);
        RequestContext context = getRequestContext();
        byte[] enrichedBytes = processor.processMetricsData(context, buffer);
        MetricsData enrichedData = MetricsData.parseFrom(enrichedBytes);

        // Assert Static metadata enrichment
        Assert.assertTrue(enrichedData.getResourceMetricsCount() > 0);
        enrichedData.getResourceMetricsList().forEach(resourceMetrics -> {
            Resource resource = resourceMetrics.getResource();
            boolean foundStatic = resource.getAttributesList().stream()
                    .anyMatch(kv -> kv.getKey().equals("staticKey") &&
                            kv.getValue().getStringValue().equals("staticValue"));
            Assert.assertTrue(foundStatic, "Missing static metadata attribute");

            // Assert Dynamic metadata enrichment
            boolean foundClientId = resource.getAttributesList().stream()
                    .anyMatch(kv -> kv.getKey().equals("clientId") &&
                            kv.getValue().getStringValue().equals("client-123"));
            Assert.assertTrue(foundClientId, "Missing dynamic metadata clientId");
            boolean foundClientSoftwareName = resource.getAttributesList().stream()
                    .anyMatch(kv -> kv.getKey().equals("clientSoftwareName") &&
                            kv.getValue().getStringValue().equals("kafkaClient"));
            Assert.assertTrue(foundClientSoftwareName, "Missing dynamic metadata clientSoftwareName");
            boolean foundClientSoftwareVersion = resource.getAttributesList().stream()
                    .anyMatch(kv -> kv.getKey().equals("clientSoftwareVersion") &&
                            kv.getValue().getStringValue().equals("1.0.0"));
            Assert.assertTrue(foundClientSoftwareVersion, "Missing dynamic metadata clientSoftwareVersion");
        });
    }

    private static RequestContext getRequestContext() {
        RequestHeader header = new RequestHeader(
                ApiKeys.METADATA,
                (short) 0,
                "client-123",
                42
        );

        KafkaPrincipal principal = new KafkaPrincipal("User", "test-user");
        ListenerName listenerName = ListenerName.forSecurityProtocol(SecurityProtocol.PLAINTEXT);
        SecurityProtocol securityProtocol = SecurityProtocol.PLAINTEXT;
        ClientInformation clientInformation = new ClientInformation("kafkaClient", "1.0.0");

        return new RequestContext(
                header,
                "conn-1",
                InetAddress.getLoopbackAddress(),
                principal,
                listenerName,
                securityProtocol,
                clientInformation,
                false
        );
    }
}
