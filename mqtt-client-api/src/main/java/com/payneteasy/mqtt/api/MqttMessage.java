package com.payneteasy.mqtt.api;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Builder
public class MqttMessage {
    String topic;
    byte[] payload;

    @Override
    public String toString() {
        return "MqttMessage{" +
                "topic='" + topic + '\'' +
                ", payload=" + new String(payload, StandardCharsets.UTF_8) +
                '}';
    }
}
