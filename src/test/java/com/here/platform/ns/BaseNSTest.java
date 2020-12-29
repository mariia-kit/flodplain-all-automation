package com.here.platform.ns;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


@Tag("ignored-prod")
@Tag("neutral_server")
@Execution(ExecutionMode.CONCURRENT)
public class BaseNSTest {

    @BeforeAll
    public static void suiteSetup1() {
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
