package com.payneteasy.mqtt.api;

public interface IMessageQueueWriter {

    void addMessage(MqttMessage aMessage);

}
