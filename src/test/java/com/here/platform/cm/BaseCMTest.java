package com.here.platform.cm;

import com.github.javafaker.Crypto;
import com.github.javafaker.Faker;
import com.here.platform.cm.controllers.ConsentRequestController;
import com.here.platform.cm.controllers.PrivateController;
import com.here.platform.cm.controllers.ProvidersController;
import com.here.platform.common.TestResultLoggerExtension;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(TestResultLoggerExtension.class)
@Tag("consent_management")
public class BaseCMTest {

    protected static Faker faker = new Faker();
    protected static Crypto crypto = faker.crypto();

    static {
        //To run on specific environment CM tests use following "env" values: dev, sit, prod
        //System.setProperty("env", "dev");

        if (System.getProperty("env").matches("dev|sit")) {
            new PrivateController().cleanUpEnvConsents();
        }
    }

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
