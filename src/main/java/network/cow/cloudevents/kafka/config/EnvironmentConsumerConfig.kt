package network.cow.cloudevents.kafka.config

/**
 * @author Benedikt Wüller
 */
class EnvironmentConsumerConfig(prefix: String) : ConsumerConfig(
    (System.getenv("${prefix}_KAFKA_BROKERS") ?: "127.0.0.1:9092").split(","),
    System.getenv("${prefix}_KAFKA_TOPICS")?.split(",") ?: emptyList(),
    System.getenv("${prefix}_KAFKA_GROUP_ID_PREFIX") ?: System.getenv("${prefix}_CLOUD_EVENT_SOURCE") ?: "cow.undefined.undefined"
)
