package com.payneteasy.mqtt.api;

import com.payneteasy.mqtt.api.impl.IMessageQueueReader;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Builder
public class MqttClientOptions {
    @NonNull Duration            connectTimeout;
    @NonNull Duration            readTimeout;
    @NonNull Duration            pullTimeout;
    @NonNull Duration            keepAlive;
    @NonNull InetSocketAddress   address;
    @NonNull List<MqttTopic>     topics;
    @NonNull IMessageQueueReader queueReader;
    @NonNull String              clientId;
    @NonNull IMqttListener       listener;
}
