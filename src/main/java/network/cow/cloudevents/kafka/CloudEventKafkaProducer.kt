package network.cow.cloudevents.kafka

import io.cloudevents.CloudEvent
import com.google.protobuf.Message
import io.cloudevents.core.v1.CloudEventBuilder
import io.cloudevents.kafka.CloudEventSerializer
import network.cow.cloudevents.kafka.config.ProducerConfig
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties
import java.util.UUID

/**
 * @author Benedikt Wüller
 */
class CloudEventKafkaProducer(private val config: ProducerConfig) {

    private val producer: KafkaProducer<String, CloudEvent>

    init {
        val properties = Properties()
        properties[BOOTSTRAP_SERVERS_CONFIG] = this.config.brokers.joinToString(",")
        properties[KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        properties[VALUE_SERIALIZER_CLASS_CONFIG] = CloudEventSerializer::class.java

        producer = KafkaProducer(properties)
    }

    fun <T : Message> send(topic: String, message: T) {
        val event = CloudEventBuilder()
            .withId(UUID.randomUUID().toString())
            .withType(message.descriptorForType.fullName)
            .withSource(this.config.sourceUri)
            .withData(message.toByteArray())
            .build()

        producer.send(ProducerRecord(topic, event))
    }

}
