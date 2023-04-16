package com.payneteasy.mqtt.client.paho;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadNames {

    private static final AtomicLong INDEX = new AtomicLong(0);

    public static String getThreadName(String aPrefix, InetSocketAddress aAddress) {
        return aPrefix + "-" + aAddress.getHostName() + "-" + aAddress.getPort() + "-" + INDEX.incrementAndGet();
    }
}
