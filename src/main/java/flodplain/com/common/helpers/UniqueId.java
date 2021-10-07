package flodplain.com.common.helpers;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.RandomStringUtils;


public class UniqueId {

    private static final AtomicInteger at = new AtomicInteger(0);
    private static final String seed = "-" + RandomStringUtils.randomNumeric(4) + Long
            .toString(Instant.now().getEpochSecond(), 32) + System.getProperties()
            .getProperty("org.gradle.test.worker");


    public static synchronized String getUniqueKey() {
        return seed + at.incrementAndGet();
    }

}
