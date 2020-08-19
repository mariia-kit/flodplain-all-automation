package com.here.platform.ns;

import com.here.platform.common.TestResultLoggerExtension;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;


@Tag("ignored-prod")
@Tag("neutral_server")
@ExtendWith(TestResultLoggerExtension.class)
public class BaseNSTest {

    private final static Logger logger = Logger.getLogger(BaseNSTest.class);

    @BeforeAll
    public static void suiteSetup1() {
    }

    static {
        //To run on specific environment NS tests use following "env" values: dev, sit, prod
        //System.setProperty("env", "dev");
    }

    @AfterAll
    public static void cleanUp() {
    }

    public static void delay(Long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
