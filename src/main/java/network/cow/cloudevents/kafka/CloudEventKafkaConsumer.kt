package network.cow.cloudevents.kafka

import com.google.protobuf.Message
import io.cloudevents.CloudEvent
import io.cloudevents.kafka.CloudEventSerializer
import network.cow.cloudevents.kafka.config.ConsumerConfig
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.CommonClientConfigs.GROUP_ID_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.util.Properties
import com.google.protobuf.Any as ProtoAny

/**
 * @author Benedikt Wüller
 */
class CloudEventKafkaConsumer(private val config: ConsumerConfig) {

    private val consumer: KafkaConsumer<String, CloudEvent>

    private val listeners = mutableMapOf<String, Pair<Class<out Message>, (Message) -> Unit>>()

    private var thread: Thread? = null

    init {
        val properties = Properties()
        properties[BOOTSTRAP_SERVERS_CONFIG] = this.config.brokers.joinToString(",")
        properties[GROUP_ID_CONFIG] = this.config.groupId
        properties[KEY_DESERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        properties[VALUE_DESERIALIZER_CLASS_CONFIG] = CloudEventSerializer::class.java

        consumer = KafkaConsumer(properties)
        consumer.subscribe(this.config.topics)
    }

    fun start() {
        if (thread != null) return
        thread = Thread {
            while (!Thread.currentThread().isInterrupted) {
                val records = consumer.poll(Duration.ofSeconds(3))
                if (records.isEmpty) continue
                records.forEach { record ->
                    val event = record.value()
                    val (type, listener) = listeners[event.type] ?: return@forEach
                    val message = ProtoAny.parseFrom(event.data.toBytes()).unpack(type)
                    listener(message)
                }
            }
        }
        thread?.start()
    }

    fun stop() {
        thread?.interrupt()
        thread = null
    }

    fun <T : Message> listen(eventType: String, dataType: Class<T>, callback: (T) -> Unit) {
        listeners[eventType] = dataType to (callback as (Message) -> Unit)
    }

}