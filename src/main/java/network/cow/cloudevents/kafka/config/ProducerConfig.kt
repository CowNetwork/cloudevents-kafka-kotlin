package network.cow.cloudevents.kafka.config

import java.net.URI

/**
 * @author Benedikt Wüller
 */
open class ProducerConfig(val brokers: List<String>, val sourceUri: URI)
