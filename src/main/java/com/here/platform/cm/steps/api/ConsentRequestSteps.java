package com.here.platform.cm.steps.api;

import com.github.javafaker.Faker;
import com.here.platform.cm.controllers.ConsentRequestController;
import com.here.platform.cm.dataAdapters.ConsentInfoToConsentRequestData;
import com.here.platform.cm.dataAdapters.ConsentRequestToConsentInfo;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.AdditionalLink;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.helpers.Steps;
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
    public ConsentInfo createValidConsentRequest(ProviderApplications targetApp, String testVin, ConsentRequestContainers container) {
        Steps.createRegularContainer(container);
        OnboardingSteps onboard = new OnboardingSteps(targetApp.provider, targetApp.consumer.getRealm());
        onboard.onboardTestProvider();
        onboard.onboardTestProviderApplication(
                container.getName(),
                targetApp.container.clientId,
                targetApp.container.clientSecret);
        return ConsentRequestSteps.createConsentRequestWithVINFor(
                targetApp.provider.getName(),
                targetApp.consumer.getConsumerName(),
                targetApp.consumer.getRealm(),
                container.getName(),
                testVin);
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
        consentRequestController.withConsumerToken(MPConsumers.OLP_CONS_1);
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
                .title(Conf.cm().getQaTestDataMarker() + faker.gameOfThrones().character())
                .purpose(faker.gameOfThrones().quote())
                .privacyPolicy(faker.internet().url())
                .addAdditionalLinksItem(
                        new AdditionalLink().title(faker.commerce().department()).url(faker.internet().url())
                )
                .containerId(containerId);

        consentRequestController.withConsumerToken();
        var consentRequestResponse = consentRequestController.createConsentRequest(targetConsentRequest);
        StepExpects.expectCREATEDStatusCode(consentRequestResponse);

        var consentRequestId = consentRequestResponse.as(ConsentRequestIdResponse.class).getConsentRequestId();
        return new ConsentRequestToConsentInfo(consentRequestId, targetConsentRequest)
                .consentInfo()
                .consumerName(consumerName)
                .privacyPolicy(targetConsentRequest.getPrivacyPolicy())
                .additionalLinks(targetConsentRequest.getAdditionalLinks());
    }

}
