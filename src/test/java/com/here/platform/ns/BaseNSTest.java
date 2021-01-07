package com.here.platform.ns;

import com.here.platform.cm.steps.remove.AllRemoveExtension;
import com.here.platform.common.annotations.CMFeatures.ZephyrComponent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


@Tag("ignored-prod")
@Tag("neutral_server")
@ZephyrComponent("NS-Service")
@Execution(ExecutionMode.CONCURRENT)
public class BaseNSTest {
    @RegisterExtension
    AllRemoveExtension consentRequestRemoveExtension = new AllRemoveExtension();

    public static void delay(Long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
