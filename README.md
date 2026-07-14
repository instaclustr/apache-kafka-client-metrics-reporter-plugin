# Apache Kafka Client Metrics Reporter Plugin

This project contains a plugin for Apache Kafka that allows you to push Kafka client metrics to different systems. The initial
implementation supports pushing metrics to an HTTP endpoint, such as to an HTTP configured OpenTelemetry Collector.
The plugin also supports adding custom metadata to the metrics, such as environment, resource ids, or any other custom tag.

This plugin leverages the [KIP-714: Client Metrics and Observability](https://cwiki.apache.org/confluence/display/KAFKA/KIP-714%3A+Client+metrics+and+observability) feature introduced in Apache Kafka 3.7.0.
This plugin is Open Source under the Apache License 2.0.
This project is maintained by [Instaclustr NetApp](https://www.instaclustr.com/).

## Prerequisites

1. Apache Kafka 4.3.1 or higher (see [Kafka Version Compatibility](#kafka-version-compatibility) for older versions).
2. Java 17 for the plugin to run.
3. Maven to build the plugin.
4. An OpenTelemetry Collector or any HTTP endpoint to push the metrics to.
5. A client application that supports [KIP-714](https://cwiki.apache.org/confluence/display/KAFKA/KIP-714%3A+Client+metrics+and+observability#KIP714:Clientmetricsandobservability-Clientbehavior) to push the metrics.
6. The client applications property `enable.metrics.push` should be set to `true`. It will be `true` by default.
7. A client application that is compatible with the version of Apache Kafka you're using.

## Using the plugin

1. Download the latest release of the plugin.
2. Compile the plugin using Maven. The in pom.xml there is currently multiple profiles to build the plugin with different versions of Apache Kafka.
By default, it will build with version 4.3.1. To build with a different version, use the `-P` flag to specify the profile.
   ```bash
   mvn clean package
   ```
   or to build with a specific profile:
   ```bash
    mvn clean package -Pkafka-4.3.1
   ```
3. Copy the generated JAR file from the `target` directory to your Apache Kafka library path. The library path is typically the `libs` directory in your Kafka installation - `$KAFKA_HOME/libs/`
4. Create a Yaml configuration file for the plugin. Below is an example configuration that pushes metrics to an OpenTelemetry Collector:
   ```yaml
   exporter: 
      mode: 'HTTP'
      endpoint: 'http://collector:4318/v1/metrics'
      timeout: 10
   metadata:
      nodeId: '123NodeID'
   ```
5. Define the environment variable `KAFKA_CLIENT_METRICS_CONFIG_PATH` to include the configuration file:
   ```bash
   export KAFKA_CLIENT_METRICS_CONFIG_PATH="/path/to/config.yaml"
   ```
6. Configure Apache Kafka with the metrics reporter. To do this, you have two options:
   1. Add the following properties to your Apache Kafka server.properties file:
      ```properties
      metric.reporters=com.instaclustr.kafka.KafkaClientMetricsReporter
      ```
   2. If you want to dynamically load the plugin run the following command:
      ```bash
      kafka-configs.sh --bootstrap-server <broker-list> --entity-type brokers --entity-name <default> --alter --add-config "metric.reporters=com.instaclustr.kafka.KafkaClientMetricsReporter"
      ```
7. Enable client metrics collection in the broker. An example command to enable the collection for producer and consumer metrics:
   ```bash
   kafka-client-metrics.sh --bootstrap-server <broker-list> \\
          --metrics org.apache.kafka.producer.,org.apache.kafka.consumer. \\
          --alter --generate-name --interval 1000
   ```
8. You can now verify that the metrics are being pushed by creating a topic and producing/consuming messages.

## Kafka Version Compatibility

### v1.2.0
The **v1.2.0 release** is requires **Apache Kafka 4.3.1** to build and run. It uses the `Compression` factory API instead of `CompressionType`, which was moved to an internal package in Kafka 4.3.0 ([KAFKA-20128](https://github.com/apache/kafka/pull/21412)).

In Kafka 4.3.1, broker startup code in [`Logging.scala`](https://github.com/apache/kafka/blob/4.3.1/core/src/main/scala/kafka/utils/Logging.scala) was changed to call `org.apache.kafka.common.utils.Utils.registerMBean` instead of the Scala `CoreUtils.registerMBean` used in Kafka 4.2.x ([`Logging.scala` in 4.2.1](https://github.com/apache/kafka/blob/4.2.1/core/src/main/scala/kafka/utils/Logging.scala)). If a **v1.1.0** jar (built with `kafka-clients` 4.2.x) is present in the broker's `libs/` directory alongside `kafka-clients-4.3.1.jar`, the JVM may load the 4.2.x classes first, causing the broker to fail at startup with:
```
java.lang.NoSuchMethodError: 'boolean org.apache.kafka.common.utils.Utils.registerMBean(java.lang.Object, java.lang.String)'
```
Replace the plugin jar with the **v1.2.0** build (compiled against `kafka-clients` 4.3.1) to resolve this.

### v1.1.0
The **v1.1.0 release** requires **Apache Kafka 4.2.0** to build and run. The current codebase uses the
[KIP-1217](https://cwiki.apache.org/confluence/display/KAFKA/KIP-1217%3A+Include+push+interval+in+ClientTelemetryReceiver+context)
interfaces (`ClientTelemetryExporter`, `ClientTelemetryExporterProvider`, `ClientTelemetryContext`) which were
introduced in Kafka 4.2.0 and are not present in earlier releases.

### v1.0.4
If you need to run the plugin against **Kafka 3.x or 4.0.x / 4.1.x**, please use the
[v1.0.4 release](https://github.com/instaclustr/apache-kafka-client-metrics-reporter-plugin/releases/tag/v1.0.4),
which is the last version built against the older `ClientTelemetry` / `ClientTelemetryReceiver` interfaces and
supports Kafka versions 3.8.1, 3.9.1, 3.9.2, 4.0.0, and 4.1.1.

## Bug Reports & Feature Requests

Please file any bugs or feature requests as GitHub issues in this repository.

## Support

For any queries related to this plugin, please feel free to contact us at [Instaclustr Support](https://www.instaclustr.com/support/).

## Copyright

© 2025 NetApp, Inc. All Rights Reserved. NETAPP, the NETAPP logo, and the marks listed at http://www.netapp.com/TM are trademarks of NetApp, Inc. in the U.S. and/or other countries. Other company and product names may be trademarks of their respective owners.
