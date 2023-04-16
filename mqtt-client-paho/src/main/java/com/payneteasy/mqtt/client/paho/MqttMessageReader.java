package com.payneteasy.mqtt.client.paho;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.eclipse.paho.mqttv5.common.packet.MqttDataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

import static lombok.AccessLevel.PRIVATE;

public class MqttMessageReader {

    private static final Logger LOG = LoggerFactory.getLogger( MqttMessageReader.class );

    private final DataInputStream in;

    public byte[] readLengthHeaderPayload() throws IOException {
        int length = MqttDataTypes.readVariableByteInteger(in).getValue();
        byte[] buf = new byte[length];
        in.readFully(buf);
        return buf;
    }

    @Data
    @FieldDefaults(makeFinal = true, level = PRIVATE)
    @Builder
    public static class FixedHeader {
        int type;
        int info;

        public byte getInfoByte() {
            return (byte) info;
        }
    }

    public MqttMessageReader(DataInputStream in) {
        this.in = in;
    }

    public FixedHeader readFixedHeader() throws IOException {
        int first = in.readUnsignedByte();
        byte type = (byte) (first >> 4);
        byte info = (byte) (first &= 0x0f);
        return new FixedHeader(type, info);
    }
}
