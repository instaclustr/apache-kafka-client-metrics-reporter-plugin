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
