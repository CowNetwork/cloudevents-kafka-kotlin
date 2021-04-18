# cloudevents-kafka-kotlin

CloudEvent wrapper for protobuf over kafka written in kotlin.

## Environment Variables

When using the `EnvironmentProducerConfig` and `EnvironmentConsumerConfig` classes, the following environment variables are being used
for configuring the producer and consumer respectively.

| Environment Variable | Default Value | Scopes | Description |
| -------------------- | ------------- | ------ | ----------- |
| `PREFIX_KAFKA_BROKERS` | `127.0.0.1:9092` | Producer, Consumer | The kafka brokers to connect to. |
| `PREFIX_CLOUD_EVENT_SOURCE` | `cow.undefined.undefined` | Producer | The source uri used for producing cloud events. |
| `PREFIX_KAFKA_TOPICS` | ` ` | Consumer | The kafka topics to listen on to receive cloud events. |
| `PREFIX_KAFKA_GROUP_ID_PREFIX` | `$PREFIX_CLOUD_EVENT_SOURCE` | Consumer | The group id prefix to prepend to the randomly generated uuid. |

The `PREFIX` will be defined by the first parameter passed to the `EnvironmentProducerConfig` and `EnvironmentConsumerConfig` classes.

## Usage

### Kotlin

```kotlin
// Defining a singleton producer.
object CloudEventProducer : CloudEventKafkaProducer(EnvironmentProducerConfig("USER_SERVICE"))

// Publish an event.
val message: PlayerMetadataUpdatedEvent = // ...
CloudEventProducer.send("cow.global.user", message)
```

```kotlin
// Defining a singleton consumer.
object CloudEventConsumer : CloudEventKafkaConsumer(EnvironmentConsumerConfig("USER_SERVICE"))

// Start the consumer.
CloudEventConsumer.start()

// Listen for the given proto message as event data.
CloudEventConsumer.listen("cow.user.v1.PlayerMetadataUpdatedEvent", PlayerMetadataUpdatedEvent::class.java) { message ->
    // Do stuff with your protobuf message.
}

// Stop the consumer.
CloudEventConsumer.stop()
```

### Java

```java
// Define a local producer.
final CloudEventKafkaProducer producer = new CloudEventKafkaProducer(new EnvironmentProducerConfig("USER_SERVICE"));

// Publish an event.
final PlayerMetadataUpdatedEvent message = // ...
producer.send("cow.global.user", message);
```

```java
// Define a local consumer.
final CloudEventKafkaConsumer consumer = new CloudEventKafkaConsumer(new EnvironmentConsumerConfig("USER_SERVICE"));

// Start the consumer.
consumer.start();

// Listen for the given proto message as event data.
consumer.listen("cow.user.v1.PlayerMetadataUpdatedEvent", PlayerMetadataUpdatedEvent.class, message -> {
    // Do stuff with your protobuf message.
});

// Stop the consumer.
consumer.stop();
```
