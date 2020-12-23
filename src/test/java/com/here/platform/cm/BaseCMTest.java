package com.here.platform.cm;

import com.github.javafaker.Crypto;
import com.github.javafaker.Faker;
import com.here.platform.cm.controllers.ConsentRequestController;
import com.here.platform.cm.controllers.ProvidersController;
import com.here.platform.cm.steps.remove.ConsentRequestRemoveExtension2;
import com.here.platform.common.TestResultLoggerExtension;
import com.here.platform.hereAccount.controllers.HereUserManagerController;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;


@ExtendWith(TestResultLoggerExtension.class)
@Tag("consent_management")
public class BaseCMTest {
    @RegisterExtension
    ConsentRequestRemoveExtension2 consentRequestRemoveExtension = new ConsentRequestRemoveExtension2();

    protected static Faker faker = new Faker();
    protected static Crypto crypto = faker.crypto();


    protected ConsentRequestController consentRequestController = new ConsentRequestController();
    protected ProvidersController providerController = new ProvidersController();
    protected final HereUserManagerController hereUserManagerController = new HereUserManagerController();

    @SneakyThrows
    protected static void fuSleep() {
        Thread.sleep(2500); //cos NS-804
    }

    @AfterEach
    void afterEach() {
        consentRequestController.clearBearerToken();
    }

}
