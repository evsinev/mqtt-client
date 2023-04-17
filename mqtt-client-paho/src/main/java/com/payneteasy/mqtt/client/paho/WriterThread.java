package com.payneteasy.mqtt.client.paho;

import com.payneteasy.mqtt.api.MqttMessage;
import com.payneteasy.mqtt.api.impl.IMessageQueueReader;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.packet.MqttPingReq;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.MqttPublish;
import org.eclipse.paho.mqttv5.common.packet.MqttWireMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;

public class WriterThread extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger( WriterThread.class );

    private final Socket              socket;
    private final IMessageQueueReader queue;
    private final Duration            pullDuration;
    private final OutputStream        out;

    private int messageId = 2;

    public WriterThread(Socket aSocket, IMessageQueueReader queueReader, Duration pullTimeout, InetSocketAddress aAddress) throws IOException {
        super(ThreadNames.getThreadName("mqtt-writer", aAddress));
        queue = queueReader;
        socket = aSocket;
        pullDuration = pullTimeout;
        out = aSocket.getOutputStream();
    }

    @Override
    public void run() {
        while(!isInterrupted() && socket.isConnected()) {
            try {
                queue.pullMessage(pullDuration).ifPresent(this::sendMessage);
                sendPing();
            } catch (InterruptedException e) {
                LOG.warn("Interrupted writer", e);
                closeSocket();
                return;
            } catch (Exception e) {
                LOG.error("Cannot write message", e);
                closeSocket();
                return;
            }
        }
    }

    private void sendPing() {
        LOG.debug("Sending ping ...");
        writeWire(new MqttPingReq());
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            LOG.error("Cannot close socket", e);
        }
    }

    private void sendMessage(MqttMessage aMessage) {
        final int messageId = createMessageId();

        org.eclipse.paho.mqttv5.common.MqttMessage pahoMessage = new org.eclipse.paho.mqttv5.common.MqttMessage(aMessage.getPayload());
        pahoMessage.setId(messageId);

        MqttPublish publish = new MqttPublish(
                aMessage.getTopic()
                , pahoMessage
                , new MqttProperties()
        );
        publish.setMessageId(messageId);
        LOG.debug("Sending message id {} to {} : {}", messageId, aMessage.getTopic(), new String(aMessage.getPayload(), UTF_8));
        writeWire(publish);
    }

    private int createMessageId() {
        messageId++;
        if(messageId > 65000) {
            messageId = 3;
        }
        return messageId;
    }

    private void writeWire(MqttWireMessage publish) {
        try {
            out.write(publish.getHeader());
            out.write(publish.getPayload());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write message", e);
        } catch (MqttException e) {
            throw new IllegalStateException("Cannot create message", e);
        }
    }
}
