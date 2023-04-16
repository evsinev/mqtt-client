package com.payneteasy.mqtt.client.paho;

import com.payneteasy.mqtt.api.IMqttClient;
import com.payneteasy.mqtt.api.MqttClientOptions;
import com.payneteasy.mqtt.api.MqttMessage;
import com.payneteasy.mqtt.api.MqttTopic;
import com.payneteasy.mqtt.api.impl.MessageQueueArrayImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofSeconds;
import static java.util.Collections.singletonList;

public class SimpleClientApp {

    private static final Logger LOG = LoggerFactory.getLogger( SimpleClientApp.class );

    public static void main(String[] args) throws InterruptedException {
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

        LOG.debug("Sleeping 240 seconds...");
        Thread.sleep(240_000);

        client.scheduleClose();
        LOG.debug("Sleeping 2 seconds...");
        Thread.sleep(2_000);
    }
}
