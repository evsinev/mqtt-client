package com.payneteasy.mqtt.api.impl;

import com.payneteasy.mqtt.api.IMessageQueueWriter;
import com.payneteasy.mqtt.api.MqttMessage;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageQueueArrayImpl implements IMessageQueueReader, IMessageQueueWriter {

    private final BlockingQueue<MqttMessage> queue;

    public MessageQueueArrayImpl(int aSize) {
        queue = new ArrayBlockingQueue<>(aSize);
    }

    @Override
    public void addMessage(MqttMessage aMessage) {
        queue.add(aMessage);
    }

    @Override
    public Optional<MqttMessage> pullMessage(Duration aTimeout) throws InterruptedException {
        MqttMessage message = queue.poll(aTimeout.toMillis(), TimeUnit.MILLISECONDS);
        return Optional.ofNullable(message);
    }
}
