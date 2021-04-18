package network.cow.cloudevents.kafka.config

import java.net.URI
import java.util.UUID

/**
 * @author Benedikt Wüller
 */
open class ConsumerConfig(brokers: List<String>, val topics: List<String>, val groupIdPrefix: String) : ProducerConfig(brokers, URI.create("cow.undefined.undefined")) {
    val groupId = "${this.groupIdPrefix}.${UUID.randomUUID()}"
}
