package com.here.platform.cm.consentStatus;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.dataAdapters.ConsentInfoToConsentRequestData;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.steps.ConsentRequestSteps;
import com.here.platform.common.VinsToFile;
import com.here.platform.dataProviders.DataSubjects;
import java.io.File;


public class BaseConsentStatusTests extends BaseCMTest {

    private final ProviderApplications targetApp = ProviderApplications.DAIMLER_CONS_1;
    protected final MPConsumers mpConsumer = targetApp.consumer;
    protected DataSubjects vehicle = DataSubjects.getNext();
    protected String
            testConsumerId = mpConsumer.getRealm(),
            testProviderId = targetApp.provider.getName(),
            testVin = vehicle.vin;

    protected File testFileWithVINs = null;

    protected ConsentRequestContainers testContainer = targetApp.container;

    protected ConsentStatusController consentStatusController = new ConsentStatusController();
    protected ConsentRequestData testConsentRequestData = new ConsentRequestData()
            .consumerId(testConsumerId)
            .providerId(testProviderId)
            .containerId(testContainer.id)
            .privacyPolicy(faker.internet().url())
            .purpose(faker.commerce().productName())
            .title(faker.gameOfThrones().quote());

    protected String createValidConsentRequest() {
        testFileWithVINs = new VinsToFile(testVin).json();
        var targetConsentRequest = ConsentRequestSteps.createConsentRequestWithVINFor(targetApp, testVin);
        testConsentRequestData = new ConsentInfoToConsentRequestData(
                targetConsentRequest, testProviderId, testConsumerId
        ).consentRequestData();
        return targetConsentRequest.getConsentRequestId();
    }

}
