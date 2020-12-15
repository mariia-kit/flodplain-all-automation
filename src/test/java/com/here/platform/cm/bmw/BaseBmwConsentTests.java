package com.here.platform.cm.bmw;

import com.github.javafaker.Faker;
import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.ConsentRequestController;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.dataAdapters.ConsentContainerToNsContainer;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentRequestAsyncUpdateInfo;
import com.here.platform.cm.rest.model.ConsentRequestAsyncUpdateInfo.StatusEnum;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.CMFeatures.BMW;
import com.here.platform.common.config.Conf;
import com.here.platform.dataProviders.reference.controllers.ReferenceProviderController;
import com.here.platform.ns.dto.User;
import io.qameta.allure.Issue;
import io.qameta.allure.Issues;
import io.restassured.response.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


@BMW
@Issues({@Issue("OLPPORT-3250"), @Issue("OLPPORT-3251")})
public class BaseBmwConsentTests extends BaseCMTest {

    protected final ProviderApplications targetApp = ProviderApplications.BMW_CONS_1;
    protected final User mpConsumer = targetApp.consumer;
    protected static Faker faker = new Faker();

    String
            testVin1 = "2AD190A6AD057824E",
            testVin2 = "2AD190A6AD0578AAA";

    protected File testFileWithVINs = null;
    protected List<String> listOfClearances = new ArrayList<>();

    protected ConsentRequestContainer testContainer = ConsentRequestContainers.generateNew(targetApp.provider)
            .withResources(List.of("fuel"))
            .withClientIdSecret(Conf.cmUsers().getBmwApp());

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
        testFileWithVINs = new VinsToFile(testVin1).json();
        testContainer.setClientId(testContainer.getId());
        new ReferenceProviderController().addContainer(new ConsentContainerToNsContainer(testContainer).nsContainer());
        String crid = ConsentRequestSteps.createValidConsentRequestWithNSOnboardings(targetApp, testVin1, testContainer)
                .getConsentRequestId();
        testConsentRequestData.setContainerId(testContainer.getId());
        return crid;
    }

    protected void waitForAsyncBMWRequest(String asyncId) {
        int maxCount = 10;
        while (maxCount > 0) {
            maxCount--;
            Response resp = consentRequestController.getConsentRequestAsyncUpdateInfo(asyncId);
            ConsentRequestAsyncUpdateInfo convertedObject = resp.as(ConsentRequestAsyncUpdateInfo.class);
            if (!convertedObject.getStatus().equals(StatusEnum.IN_PROGRESS)) {
                break;
            }
            fuSleep();
        }

    }

}
