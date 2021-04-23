package network.cow.cloudevents.kafka

import com.google.protobuf.Message
import com.google.protobuf.Parser
import io.cloudevents.CloudEvent
import io.cloudevents.kafka.CloudEventDeserializer
import network.cow.cloudevents.kafka.config.ConsumerConfig
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.CommonClientConfigs.GROUP_ID_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.Properties
import com.google.protobuf.Any as ProtoAny

/**
 * @author Benedikt Wüller
 */
open class CloudEventKafkaConsumer(private val config: ConsumerConfig) {

    private val parsers = mutableMapOf<Class<out Message>, Parser<out Message>>()

    private val consumer: KafkaConsumer<String, CloudEvent>

    private val listeners = mutableMapOf<String, MutableList<Pair<Class<out Message>, (Message) -> Unit>>>()

    private var thread: Thread? = null

    init {
        val properties = Properties()
        properties[BOOTSTRAP_SERVERS_CONFIG] = this.config.brokers.joinToString(",")
        properties[GROUP_ID_CONFIG] = this.config.groupId
        properties[KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        properties[VALUE_DESERIALIZER_CLASS_CONFIG] = CloudEventDeserializer::class.java

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
                    val pairs = listeners[event.type] ?: return@forEach
                    pairs.forEach { (type, listener) ->
                        try {
                            val parser = this.parsers.getOrPut(type) {
                                val method = type.getMethod("parser")
                                return@getOrPut method.invoke(null) as Parser<out Message>
                            }

                            val message = parser.parseFrom(event.data.toBytes())
                            listener(message)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
            }
        }
        thread?.start()
    }

    fun stop() {
        thread?.interrupt()
        thread = null
    }

    fun <T : Message> listen(eventType: String, dataType: Class<T>, callback: (T) -> Unit) : Pair<Class<T>, (T) -> Unit> {
        val listeners = this.listeners.getOrPut(eventType) { mutableListOf() }
        val pair = dataType to (callback as (Message) -> Unit)
        listeners.add(pair)
        return pair
    }

    fun unregister(eventType: String, pair: Pair<Class<out Message>, (Message) -> Unit>) {
        val listeners = this.listeners[eventType] ?: return
        listeners.remove(pair)
    }

    fun clear() {
        this.listeners.clear()
    }

}
