package com.payneteasy.mqtt.api.impl;

import com.payneteasy.mqtt.api.MqttMessage;

import java.time.Duration;
import java.util.Optional;

public interface IMessageQueueReader {

    Optional<MqttMessage> pullMessage(Duration aTimeout) throws InterruptedException;

}
