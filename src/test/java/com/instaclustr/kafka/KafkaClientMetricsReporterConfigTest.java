package com.instaclustr.kafka;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;

public class KafkaClientMetricsReporterConfigTest {


    @Test
    public void testConfigLoadsSuccessfully() throws Exception {
        String yamlContent = "key1: value1\nkey2: value2";
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-config", ".yaml");
        java.nio.file.Files.write(tempFile, yamlContent.getBytes());

        try {
            KafkaClientMetricsReporterConfig config = new KafkaClientMetricsReporterConfig(tempFile.toString());
            Assert.assertNotNull(config.configurations);
            Assert.assertEquals(config.configurations.get("key1"), "value1");
            Assert.assertEquals(config.configurations.get("key2"), "value2");
        } finally {
            java.nio.file.Files.deleteIfExists(tempFile);
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testConfigThrowsOnMissingFile() {
        String fakePath = "/tmp/missing.yaml";
        try (MockedStatic<System> systemMock = Mockito.mockStatic(System.class)) {
            systemMock.when(() -> System.getenv("KAFKA_CLIENT_METRICS_CONFIG_PATH")).thenReturn(fakePath);
            new KafkaClientMetricsReporterConfig();
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testConfigThrowsOnInvalidYaml() throws Exception {
        String invalidYaml = "::::";
        String fakePath = "/tmp/invalid.yaml";

        try (MockedStatic<System> systemMock = Mockito.mockStatic(System.class);
             MockedStatic<FileInputStream> fisMock = Mockito.mockStatic(FileInputStream.class)) {

            systemMock.when(() -> System.getenv("KAFKA_CLIENT_METRICS_CONFIG_PATH")).thenReturn(fakePath);

            ByteArrayInputStream bais = new ByteArrayInputStream(invalidYaml.getBytes());
            FileInputStream fis = Mockito.mock(FileInputStream.class);
            Mockito.when(fis.read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt()))
                    .thenAnswer(invocation -> bais.read(
                            invocation.getArgument(0),
                            invocation.getArgument(1),
                            invocation.getArgument(2)
                    ));
            Mockito.when(fis.read()).thenAnswer(invocation -> bais.read());
            fisMock.when(() -> new FileInputStream(fakePath)).thenReturn(fis);

            new KafkaClientMetricsReporterConfig();
        }
    }
}
