package com.here.platform.e2e;

import com.github.javafaker.Faker;
import com.here.platform.cm.steps.remove.AllRemoveExtension;
import com.here.platform.common.annotations.CMFeatures.ZephyrComponent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


@Tag("e2e")
@Disabled
@ZephyrComponent("NS-CM-E2E-Test")
@Execution(ExecutionMode.CONCURRENT)
public class BaseE2ETest {
    @RegisterExtension
    AllRemoveExtension consentRequestRemoveExtension = new AllRemoveExtension();

    Faker faker = new Faker();

}
