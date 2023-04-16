package com.payneteasy.mqtt.client.paho;

import com.payneteasy.mqtt.api.IMqttClient;
import com.payneteasy.mqtt.api.MqttClientOptions;

public class MqttClientPaho implements IMqttClient {

    private final ConnectAndReadThread connectAndReadThread;

    public MqttClientPaho(MqttClientOptions options) {
        connectAndReadThread = new ConnectAndReadThread(options);
        connectAndReadThread.start();
    }

    @Override
    public void scheduleClose() {
        connectAndReadThread.stopSocket();
    }
}
