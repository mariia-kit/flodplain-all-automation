package com.here.platform.cm.steps;

import com.github.javafaker.Faker;
import com.here.platform.cm.controllers.ConsentRequestController;
import com.here.platform.cm.dataAdapters.ConsentRequestToConsentInfo;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.AdditionalLink;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;


@UtilityClass
public class ConsentRequestSteps {

    private final Faker faker = new Faker();
    private final ConsentRequestController consentRequestController = new ConsentRequestController();

    //TODO fix duplications for consent request creation
    @Step
    public ConsentInfo createConsentRequestWithVINFor(ProviderApplications providerApplication, String vin) {
        ConsentInfo consentInfo = createConsentRequestFor(providerApplication);

        addVINsToConsentRequest(providerApplication, consentInfo.getConsentRequestId(), vin);

        return consentInfo.vinLabel(new VIN(vin).label());
    }

    @Step
    public void addVINsToConsentRequest(ProviderApplications providerApplication, String crid, String... vins) {
        consentRequestController.withConsumerToken(providerApplication.consumer);
        var addVINsResponse = consentRequestController.addVinsToConsentRequest(
                crid, new VinsToFile(vins).csv()
        );
        StepExpects.expectOKStatusCode(addVINsResponse);
    }

    @Step
    public ConsentInfo createConsentRequestFor(ProviderApplications providerApplication) {
        var targetConsentRequest = new ConsentRequestData()
                .providerId(providerApplication.provider.getName())
                .consumerId(providerApplication.consumer.getRealm())
                .title(faker.gameOfThrones().character())
                .purpose(faker.gameOfThrones().quote())
                .privacyPolicy(faker.internet().url())
                .addAdditionalLinksItem(
                        new AdditionalLink().title(faker.commerce().department()).url(faker.internet().url())
                )
                .containerId(providerApplication.container.id);

        consentRequestController.withCMToken();
        var consentRequestResponse = consentRequestController.createConsentRequest(targetConsentRequest);
        StepExpects.expectCREATEDStatusCode(consentRequestResponse);

        var consentRequestId = consentRequestResponse.as(ConsentRequestIdResponse.class).getConsentRequestId();
        return new ConsentRequestToConsentInfo(consentRequestId, targetConsentRequest).consentInfo();
    }

}
