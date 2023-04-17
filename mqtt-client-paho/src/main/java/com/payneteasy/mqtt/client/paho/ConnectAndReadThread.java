package com.payneteasy.mqtt.client.paho;

import com.payneteasy.mqtt.api.IMqttListener;
import com.payneteasy.mqtt.api.MqttClientOptions;
import com.payneteasy.mqtt.api.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

import static org.eclipse.paho.mqttv5.common.packet.MqttWireMessage.*;

public class ConnectAndReadThread extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger( ConnectAndReadThread.class );

    private final MqttClientOptions       options;
    private final AtomicReference<Socket> socketRef = new AtomicReference<>();

    public ConnectAndReadThread(MqttClientOptions options) {
        super(ThreadNames.getThreadName("mqtt-connect-read", options.getAddress()));
        this.options = options;
    }

    @Override
    public void run() {
        IMqttListener listener = new SafeMqttListener(options.getListener());
        while (!isInterrupted()) {
            try {
                final Socket socket;
                try {
                    listener.onConnecting();
                    socket = connect();
                    socketRef.set(socket);
                    listener.onConnected();
                } catch (IOException e) {
                    listener.onConnectError();
                    LOG.error("Cannot connect to {}", options.getAddress(), e);
                    try {
                        Thread.sleep(1_000);
                    } catch (InterruptedException ex) {
                        LOG.warn("Interrupted after connect", ex);
                        return;
                    }
                    continue;
                }

                try {
                    startReadingLoop(socket);
                } finally {
                    socketRef.set(null);
                    socket.close();
                }
            } catch (Exception e) {
                if(isInterrupted()) {
                    LOG.warn("Exiting on interrupt and socket close");
                    return;
                }
                LOG.error("Error while reading", e);
                try {
                    sleep(1_000);
                } catch (InterruptedException ex) {
                    LOG.error("Interrupted after read", ex);
                    return;
                }
            }
        }
    }

    private void startReadingLoop(Socket aSocket) throws IOException, MqttException {

        DataInputStream   dataInputStream     = new DataInputStream(aSocket.getInputStream());
        SimplePahoConnectionStarter connection = new SimplePahoConnectionStarter(aSocket, dataInputStream);
        connection.configureMqttConnection(options);

        WriterThread writerThread = new WriterThread(aSocket, options.getQueueReader(), options.getPullTimeout(), options.getAddress());
        writerThread.start();
        try {
            MqttMessageReader reader = new MqttMessageReader(dataInputStream);
            while(!isInterrupted()) {
                MqttMessageReader.FixedHeader header = reader.readFixedHeader();
                checkMessageType(header);
                byte[] bytes = reader.readLengthHeaderPayload();
                processMessage(header, bytes);
            }
        } finally {
            writerThread.interrupt();
        }

    }

    private void processMessage(MqttMessageReader.FixedHeader aHeader, byte[] aBytes) throws MqttException, IOException {
        switch (aHeader.getType()) {
            case MqttWireMessage.MESSAGE_TYPE_PUBLISH:
                processPublish(aHeader, aBytes);
                break;

            case MESSAGE_TYPE_PINGRESP:
                processPing();
                break;

            case MESSAGE_TYPE_DISCONNECT:
                processDisconnect(aHeader, aBytes);
                break;
            case MESSAGE_TYPE_PUBACK:
                processPubAck(aHeader, aBytes);
                break;

            default:
                throw new IllegalStateException("Unknown message type " + aHeader.getType());
        }
    }

    private void processPubAck(MqttMessageReader.FixedHeader aHeader, byte[] aBytes) throws MqttException, IOException {
        MqttPubAck pubAck = new MqttPubAck(aBytes);
        options.getListener().onMessageSent(pubAck.getMessageId());
    }

    private void processDisconnect(MqttMessageReader.FixedHeader aHeader, byte[] aBytes) throws MqttException, IOException {
        MqttDisconnect disconnect = new MqttDisconnect(aBytes);
        throw new IllegalStateException("Disconnected " + disconnect.getReturnCode());
    }

    private void processPing() {
        LOG.info("Ping response");
    }

    private void processPublish(MqttMessageReader.FixedHeader aHeader, byte[] aBytes) throws MqttException, IOException {
        MqttPublish mqttPublish = new MqttPublish(aHeader.getInfoByte(), aBytes);
        LOG.info("Received message from topic {}", mqttPublish.getTopicName());
        MqttMessage message = MqttMessage.builder()
                .topic(mqttPublish.getTopicName())
                .payload(mqttPublish.getPayloadBytes())
                .build();
        options.getListener().onMessageReceived(message);
    }

    private void checkMessageType(MqttMessageReader.FixedHeader header) {

    }

    private Socket connect() throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout((int) options.getReadTimeout().toMillis());
        socket.connect(options.getAddress(), (int) options.getConnectTimeout().toMillis());
        return socket;
    }

    public void stopSocket() {
        interrupt();
        Socket socket = socketRef.get();
        if(socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                LOG.error("Cannot close socket", e);
            }
        }
    }
}
