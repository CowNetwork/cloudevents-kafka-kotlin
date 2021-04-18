package network.cow.cloudevents.kafka.config

import java.net.URI

/**
 * @author Benedikt Wüller
 */
class EnvironmentProducerConfig(prefix: String) : ProducerConfig(
    (System.getenv("${prefix}_KAFKA_BROKERS") ?: "127.0.0.1:9092").split(","),
    URI.create(System.getenv("${prefix}_CLOUD_EVENT_SOURCE") ?: "cow.undefined.undefined")
)
