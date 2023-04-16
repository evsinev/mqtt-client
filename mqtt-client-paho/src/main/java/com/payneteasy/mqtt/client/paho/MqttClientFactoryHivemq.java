package com.payneteasy.mqtt.client.paho;

import com.payneteasy.mqtt.api.IMqttClient;
import com.payneteasy.mqtt.api.IMqttClientFactory;
import com.payneteasy.mqtt.api.MqttClientOptions;

public class MqttClientFactoryHivemq implements IMqttClientFactory {

    @Override
    public IMqttClient startClient(MqttClientOptions aOptions) {
        return new MqttClientPaho(aOptions);
    }
}
