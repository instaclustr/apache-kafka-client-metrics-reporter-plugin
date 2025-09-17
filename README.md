# Apache Kafka Client Metrics Reporter Plugin

This project contains a plugin for Apache Kafka that allows you to push Kafka client metrics to different systems. The initial
implementation supports pushing metrics to an HTTP endpoint, such as to an HTTP configured OpenTelemetry Collector.
The plugin also supports adding custom metadata to the metrics, such as environment, resource ids, or any other custom tag.

This plugin leverages the [KIP-714: Client Metrics and Observability](https://cwiki.apache.org/confluence/display/KAFKA/KIP-714%3A+Client+metrics+and+observability) feature introduced in Apache Kafka 3.7.0.
This plugin is Open Source under the Apache License 2.0.
This project is maintained by [Instaclustr NetApp](https://www.instaclustr.com/).

## Using the plugin

1. Download the latest release of the plugin.
2. Compile the plugin using Maven:
   ```bash
   mvn clean package
   ```
3. Copy the generated JAR file from the `target` directory to your Apache Kafka classpath.
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

## Bug Reports & Feature Requests

Please file any bugs or feature requests as GitHub issues in this repository.

## Support

For any queries related to this plugin, please feel free to contact us at [Instaclustr Support](https://www.instaclustr.com/support/).

## Copyright

© 2025 NetApp, Inc. All Rights Reserved. NETAPP, the NETAPP logo, and the marks listed at http://www.netapp.com/TM are trademarks of NetApp, Inc. in the U.S. and/or other countries. Other company and product names may be trademarks of their respective owners.
