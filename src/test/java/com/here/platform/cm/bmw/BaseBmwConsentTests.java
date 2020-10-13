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
import com.here.platform.common.config.Conf;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import java.io.File;
import java.util.List;


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
    protected ConsentRequestContainers testContainer = ConsentRequestContainers.generateNew(targetApp.provider.getName());

    protected ConsentStatusController consentStatusController = new ConsentStatusController();
    protected ConsentRequestController consentRequestController = new ConsentRequestController();
    protected ConsentRequestData testConsentRequestData = new ConsentRequestData()
            .consumerId(targetApp.consumer.getRealm())
            .providerId(targetApp.provider.getName())
            .containerId(testContainer.getId())
            .privacyPolicy(faker.internet().url())
            .purpose(faker.commerce().productName())
            .title(Conf.cm().getQaTestDataMarker() + faker.gameOfThrones().quote());


    protected String createValidBmwConsentRequest() {
        testFileWithVINs = new VinsToFile(testVin).json();
        testContainer.resources = List.of("fuel");
        String crid = ConsentRequestSteps.createValidConsentRequest(targetApp, testVin, testContainer).getConsentRequestId();
        testConsentRequestData.setContainerId(testContainer.getId());
        return crid;
    }

}
