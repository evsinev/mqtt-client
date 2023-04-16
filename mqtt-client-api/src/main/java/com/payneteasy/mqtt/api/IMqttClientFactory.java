package com.payneteasy.mqtt.api;

public interface IMqttClientFactory {

    IMqttClient startClient(MqttClientOptions aOptions);

}
