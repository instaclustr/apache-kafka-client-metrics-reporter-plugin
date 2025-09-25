package com.instaclustr.kafka.exporters;

import com.instaclustr.kafka.helpers.MetricsMetaDataProcessor;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.telemetry.ClientTelemetryPayload;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HttpMetricsExporterTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private AuthorizableRequestContext mockRequestContext;

    @Mock
    private ClientTelemetryPayload mockPayload;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExportSendsHttpRequest() throws Exception {
        String endpoint = "http://localhost:8080/metrics";
        int timeoutMillis = 1000;
        Map<String, Object> metadata = Collections.singletonMap("testKey", "testValue");
        byte[] dummyData = new byte[]{1, 2, 3};

        when(mockPayload.data()).thenReturn(ByteBuffer.wrap(dummyData));
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(HttpResponse.class)));

        HttpMetricsExporter exporter = new HttpMetricsExporter(endpoint, timeoutMillis, metadata);

        java.lang.reflect.Field clientField = HttpMetricsExporter.class.getDeclaredField("httpClient");
        clientField.setAccessible(true);
        clientField.set(exporter, mockHttpClient);

        exporter.export(mockRequestContext, mockPayload);
        verify(mockHttpClient, times(1)).sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }





}
