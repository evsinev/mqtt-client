package com.payneteasy.mqtt.client.paho;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.payneteasy.mqtt.api.IMqttListener;
import com.payneteasy.mqtt.api.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class SimpleListener implements IMqttListener  {

    private static final Logger LOG = LoggerFactory.getLogger( SimpleListener.class );

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onMessageReceived(MqttMessage aMessage) {
        String json = new String(aMessage.getPayload(), StandardCharsets.UTF_8);
        JsonElement jsonElement = JsonParser.parseString(json);
        LOG.info("onMessageReceived() {}\n{}", aMessage.getTopic(), gson.toJson(jsonElement));

    }

    @Override
    public void onConnecting() {
        LOG.info("onConnecting()");
    }

    @Override
    public void onConnected() {
        LOG.info("onConnected()");
    }

    @Override
    public void onConnectError() {
        LOG.info("onConnectError()");
    }

    @Override
    public void onDisconnected() {
        LOG.info("onDisconnected()");
    }
}
