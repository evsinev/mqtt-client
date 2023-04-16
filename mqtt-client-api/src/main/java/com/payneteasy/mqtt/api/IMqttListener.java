package com.payneteasy.mqtt.api;

public interface IMqttListener {

    void onMessageReceived(MqttMessage aMessage);

    void onConnecting();

    void onConnected();

    void onConnectError();

    void onDisconnected();
}
