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
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import java.io.File;


public class BaseBmwConsentTests extends BaseCMTest {

    private final ProviderApplications targetApp = ProviderApplications.BMW_CONS_1;
    protected final MPConsumers mpConsumer = targetApp.consumer;
    protected static Faker faker = new Faker();

    String
            testVin = "2AD190A6AD057824E",
            testVin1 = "2AD190A6AD0578AAA",
            bmwContainer = "S00I000M001OK";

    protected File testFileWithVINs = null;

    protected DataSubjects dataSubject = DataSubjects._2AD190A6AD057824E;
    Container container = Containers.generateNew(targetApp.provider.getName()).withResourceNames("fuel");
    protected ConsentRequestContainers testContainer = ConsentRequestContainers.getById(container.getId());

    protected ConsentStatusController consentStatusController = new ConsentStatusController();
    protected ConsentRequestController consentRequestController = new ConsentRequestController();
    protected ConsentRequestData testConsentRequestData = new ConsentRequestData()
            .consumerId(targetApp.consumer.getRealm())
            .providerId(targetApp.provider.getName())
            .containerId(container.getId())
            .privacyPolicy(faker.internet().url())
            .purpose(faker.commerce().productName())
            .title(faker.gameOfThrones().quote());


    protected String createValidBmwConsentRequest() {
        testFileWithVINs = new VinsToFile(testVin).json();
        String crid = ConsentRequestSteps.createValidConsentRequest(targetApp, testVin, container).getConsentRequestId();
        testConsentRequestData.setContainerId(container.getId());
        return crid;
    }

}
