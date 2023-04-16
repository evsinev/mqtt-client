package com.payneteasy.mqtt.client.paho;

import com.payneteasy.mqtt.api.MqttClientOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static org.eclipse.paho.mqttv5.common.packet.MqttWireMessage.MESSAGE_TYPE_DISCONNECT;

public class SimplePahoConnectionStarter {

    private static final Logger LOG = LoggerFactory.getLogger(SimplePahoConnectionStarter.class);

    private final MqttMessageReader messageReader;
    private final OutputStream      out;

    public SimplePahoConnectionStarter(Socket socket, DataInputStream aDataInputStream) throws IOException {
        messageReader = new MqttMessageReader(aDataInputStream);
        out           = socket.getOutputStream();
    }

    public void configureMqttConnection(MqttClientOptions options) throws MqttException, IOException {
        writeWire(createMqttConnect(options));
        MqttConnAck connAck = readMqttWireMessage(MqttWireMessage.MESSAGE_TYPE_CONNACK);

        writeWire(createSubscribeRequest(options));
        MqttSubAck subAck = readMqttWireMessage(MqttWireMessage.MESSAGE_TYPE_SUBACK);

    }

    private MqttSubscribe createSubscribeRequest(MqttClientOptions options) {
        MqttSubscription[] subscriptions = options.getTopics().stream()
                .map(topic -> new MqttSubscription(topic.getTopicName()))
                .toArray(MqttSubscription[]::new);

        MqttProperties properties    = new MqttProperties();
        MqttSubscribe  mqttSubscribe = new MqttSubscribe(subscriptions, properties);
        mqttSubscribe.setMessageId(2);
        return mqttSubscribe;
    }

    private <T extends MqttWireMessage> T readMqttWireMessage(int aExpectedType) throws IOException, MqttException {
        LOG.debug("Reading {} ...", aExpectedType);
        MqttMessageReader.FixedHeader fixedHeader = messageReader.readFixedHeader();
        int                           type        = fixedHeader.getType();

        if (type == MESSAGE_TYPE_DISCONNECT) {
            byte[]         buf            = messageReader.readLengthHeaderPayload();
            MqttDisconnect mqttDisconnect = new MqttDisconnect(buf);
            throw new IllegalStateException("Received disconnected message " + mqttDisconnect);
        }
        if (type != aExpectedType) {
            throw new IllegalStateException("Expected type " + aExpectedType + " but was " + type);
        }

        byte[] buf = messageReader.readLengthHeaderPayload();

        T response;
        switch (type) {
            case MqttWireMessage.MESSAGE_TYPE_CONNACK:
                return (T) new MqttConnAck(buf);
            case MqttWireMessage.MESSAGE_TYPE_SUBACK:
                return (T) new MqttSubAck(buf);

            default:
                throw new IllegalStateException("Unknown message type " + type);
        }
    }

    private void writeWire(MqttWireMessage connect) throws MqttException, IOException {
        LOG.debug("Writing {}", connect);
        out.write(connect.getHeader());
        out.write(connect.getPayload());
    }

    private MqttConnect createMqttConnect(MqttClientOptions options) {
        MqttProperties properties     = new MqttProperties();
        MqttProperties willProperties = new MqttProperties();
        MqttConnect connect = new MqttConnect(
                options.getClientId()
                , 5
                , true
                , (int) options.getKeepAlive().getSeconds()
                , properties
                , willProperties
        );
        connect.setMessageId(1);
        return connect;
    }


}
