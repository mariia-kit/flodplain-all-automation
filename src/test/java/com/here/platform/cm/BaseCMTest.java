package com.here.platform.cm;

import com.github.javafaker.Crypto;
import com.github.javafaker.Faker;
import com.here.platform.cm.controllers.ConsentRequestController;
import com.here.platform.cm.controllers.ProvidersController;
import com.here.platform.cm.steps.remove.AllRemoveExtension;
import com.here.platform.common.annotations.CMFeatures.ZephyrComponent;
import lombok.SneakyThrows;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.rules.TestName;


@Tag("consent_management")
@ZephyrComponent("CM-Service")
@Execution(ExecutionMode.CONCURRENT)
public class BaseCMTest {
    @RegisterExtension
    AllRemoveExtension allRemoveExtension = new AllRemoveExtension();
    @Rule
    public TestName testName = new TestName();

    protected static Faker faker = new Faker();
    protected static Crypto crypto = faker.crypto();


    protected ConsentRequestController consentRequestController = new ConsentRequestController();
    protected ProvidersController providerController = new ProvidersController();

    @SneakyThrows
    protected static void fuSleep() {
        Thread.sleep(2500); //cos NS-804
    }

    @AfterEach
    void afterEach() {
        consentRequestController.clearBearerToken();
    }

}
