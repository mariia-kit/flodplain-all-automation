package com.here.platform.cm.bmw;

import com.github.javafaker.Faker;
import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.ConsentRequestController;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.dataAdapters.ConsentInfoToConsentRequestData;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.common.VinsToFile;
import com.here.platform.dataProviders.daimler.DataSubjects;
import java.io.File;


public class BaseBmwConsentTests extends BaseCMTest {

    private final ProviderApplications targetApp = ProviderApplications.BMW_CONS_1;
    protected final MPConsumers mpConsumer = targetApp.consumer;
    protected DataSubjects dataSubject = DataSubjects.getNext();
    protected static Faker faker = new Faker();

    String
            testVin = "2AD190A6AD057824E",
            testVin1 = "2AD190A6AD0578AAA",
            bmwContainer = "S00I000M001OK",
            testClearanceId = "11111111-1111-1111-1111-111111111111";

    protected File testFileWithVINs = null;

    protected ConsentRequestContainers testContainer = targetApp.container;

    protected ConsentStatusController consentStatusController = new ConsentStatusController();
    protected ConsentRequestController consentRequestController = new ConsentRequestController();
    protected ConsentRequestData testConsentRequestData = new ConsentRequestData()
            .consumerId(targetApp.consumer.getRealm())
            .providerId(targetApp.provider.getName())
            .containerId(targetApp.container.id)
            .privacyPolicy(faker.internet().url())
            .purpose(faker.commerce().productName())
            .title(faker.gameOfThrones().quote());


    protected String createValidBmwConsentRequest() {
        testFileWithVINs = new VinsToFile(testVin).json();
        var targetConsentRequest = ConsentRequestSteps.createConsentRequestWithVINFor(targetApp, testVin);
        testConsentRequestData = new ConsentInfoToConsentRequestData(
                targetConsentRequest, targetApp.provider.getName(), targetApp.consumer.getRealm()
        ).consentRequestData();
        return targetConsentRequest.getConsentRequestId();
    }
}
