package com.here.platform.ns.helpers;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.RandomStringUtils;


public class UniqueId {

    private static AtomicInteger at = new AtomicInteger(0);
    private static String seed = "-" + RandomStringUtils.randomNumeric(4) + Long
            .toString(Instant.now().getEpochSecond(), 32) + System.getProperties().getProperty("org.gradle.test.worker");


    public static synchronized String getUniqueKey() {
        return seed + at.incrementAndGet();
    }

}
