package com.payneteasy.mqtt.client.paho;

import com.payneteasy.mqtt.api.IMqttListener;
import com.payneteasy.mqtt.api.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeMqttListener implements IMqttListener {

    private static final Logger LOG = LoggerFactory.getLogger( SafeMqttListener.class );

    private final IMqttListener delegate;

    public SafeMqttListener(IMqttListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onMessageReceived(MqttMessage aMessage) {
        try {
            delegate.onMessageReceived(aMessage);
        } catch (Exception e) {
            LOG.error("Cannot onMessageReceived()", e);
        }
    }

    @Override
    public void onConnecting() {
        try {
            delegate.onConnecting();
        } catch (Exception e) {
            LOG.error("Cannot onConnecting()", e);
        }
    }

    @Override
    public void onConnected() {
        try {
            delegate.onConnected();
        } catch (Exception e) {
            LOG.error("Cannot onConnected()", e);
        }
    }

    @Override
    public void onConnectError() {
        try {
            delegate.onConnectError();
        } catch (Exception e) {
            LOG.error("Cannot onConnectError()", e);
        }
    }

    @Override
    public void onDisconnected() {
        try {
            delegate.onDisconnected();
        } catch (Exception e) {
            LOG.error("Cannot onDisconnected()", e);
        }
    }
}
