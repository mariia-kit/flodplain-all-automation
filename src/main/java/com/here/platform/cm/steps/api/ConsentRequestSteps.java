package com.here.platform.cm.steps.api;

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
    public ConsentInfo createConsentRequestWithVINFor(String providerName, String consumerName, String consumerRealm, String containerId, String vin) {
        ConsentInfo consentInfo = createConsentRequestFor(providerName, consumerName, consumerRealm, containerId);
        addVINsToConsentRequestAsConsumer(consentInfo.getConsentRequestId(), vin);
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
    public void addVINsToConsentRequestAsConsumer(String crid, String... vins) {
        consentRequestController.withConsumerToken();
        var addVINsResponse = consentRequestController.addVinsToConsentRequest(
                crid, new VinsToFile(vins).csv()
        );
        StepExpects.expectOKStatusCode(addVINsResponse);
    }

    @Step
    public ConsentInfo createConsentRequestFor(ProviderApplications providerApplication) {
        return createConsentRequestFor(
                providerApplication.provider.getName(),
                providerApplication.consumer.getConsumerName(),
                providerApplication.consumer.getRealm(),
                providerApplication.container.id);
    }

    @Step
    public ConsentInfo createConsentRequestFor(String providerName, String consumerName, String consumerRealm, String containerId) {
        var targetConsentRequest = new ConsentRequestData()
                .providerId(providerName)
                .consumerId(consumerRealm)
                .title(faker.gameOfThrones().character())
                .purpose(faker.gameOfThrones().quote())
                .privacyPolicy(faker.internet().url())
                .addAdditionalLinksItem(
                        new AdditionalLink().title(faker.commerce().department()).url(faker.internet().url())
                )
                .containerId(containerId);

        consentRequestController.withConsumerToken();
        var consentRequestResponse = consentRequestController.createConsentRequest(targetConsentRequest);
        StepExpects.expectCREATEDStatusCode(consentRequestResponse);

        var consentRequestId =  consentRequestResponse.as(ConsentRequestIdResponse.class).getConsentRequestId();
        return new ConsentRequestToConsentInfo(consentRequestId, targetConsentRequest)
                .consentInfo()
                .consumerName(consumerName);
    }

}
