package com.here.platform.ns;

import com.here.platform.cm.steps.remove.ConsentRequestRemoveExtension2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


@Tag("ignored-prod")
@Tag("neutral_server")
@Execution(ExecutionMode.CONCURRENT)
public class BaseNSTest {
    @RegisterExtension
    ConsentRequestRemoveExtension2 consentRequestRemoveExtension = new ConsentRequestRemoveExtension2();

    public static void delay(Long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
