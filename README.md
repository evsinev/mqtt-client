## Simple mqtt client based on paho messages

## Feature

* This client fixed a problem with the original paho library which sometimes stops to receive messages froma a broker

## Example

```java
MessageQueueArrayImpl queue = new MessageQueueArrayImpl(100);
MqttClientOptions options = MqttClientOptions.builder()
        .address        ( new InetSocketAddress("192.168.1.70", 1883))
        .connectTimeout ( ofSeconds(30)       )
        .readTimeout    ( ofSeconds(70)       )
        .keepAlive      ( ofSeconds(60)       )
        .pullTimeout    ( ofSeconds(50)       )
        .queueReader    ( queue               )
        .clientId       ( "client-03-macbook" )
        .topics         ( singletonList(MqttTopic.builder().topicName("zigbee2mqtt/#").build()))
        .listener       ( new SimpleListener())
        .build();

IMqttClient client = new MqttClientFactoryHivemq().startClient(options);

queue.addMessage(MqttMessage.builder()
                .topic("zigbee2mqtt/laundry_switch/set")
                .payload("{\"state\" : \"OFF\"}".getBytes(UTF_8))
        .build());
```
