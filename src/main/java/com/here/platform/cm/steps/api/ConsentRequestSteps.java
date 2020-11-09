package com.here.platform.cm.steps.api;

import com.github.javafaker.Faker;
import com.here.platform.cm.controllers.ConsentRequestController;
import com.here.platform.cm.dataAdapters.ConsentRequestToConsentInfo;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.AdditionalLink;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.config.Conf;
import com.here.platform.common.strings.VIN;
import com.here.platform.ns.helpers.Steps;
import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;


@UtilityClass
public class ConsentRequestSteps {

    private final Faker faker = new Faker();
    private final ConsentRequestController consentRequestController = new ConsentRequestController();

    @Step("Create valid consent request for VIN: '{vin}'")
    public ConsentInfo createConsentRequestWithVINFor(String providerId, String consumerName, String consumerRealm,
            String containerId, String vin) {
        ConsentInfo consentInfo = createConsentRequestFor(providerId, consumerName, consumerRealm, containerId);
        addVINsToConsentRequestAsConsumer(consentInfo.getConsentRequestId(), vin);
        return consentInfo.vinLabel(new VIN(vin).label());
    }

    @Step("Onboard provider with containers on NS and CM, and create consent request")
    public ConsentInfo createValidConsentRequestWithNSOnboardings(ProviderApplications targetApp, String testVin,
            ConsentRequestContainer container) {
        Steps.createRegularContainer(container);
        OnboardingSteps onboard = new OnboardingSteps(targetApp.provider, targetApp.consumer.getRealm());
        onboard.onboardTestProvider();
        onboard.onboardValidConsumer();
        onboard.onboardTestProviderApplication(
                container.getId(),
                container.getClientId(),
                container.getClientSecret());
        return ConsentRequestSteps.createConsentRequestWithVINFor(
                targetApp.provider.getName(),
                targetApp.consumer.getConsumerName(),
                targetApp.consumer.getRealm(),
                container.getId(),
                testVin);
    }

    @Step("Add VINs: '{vins}', to consent request: '{crid}'")
    public void addVINsToConsentRequest(ProviderApplications providerApplication, String crid, String... vins) {
        consentRequestController.withConsumerToken(providerApplication.consumer);
        var addVINsResponse = consentRequestController.addVinsToConsentRequest(
                crid, new VinsToFile(vins).csv()
        );
        StatusCodeExpects.expectOKStatusCode(addVINsResponse);
    }

    @Step("Add VINs: '{vins}', to consent request: '{crid}'")
    public void addVINsToConsentRequestAsConsumer(String crid, String... vins) {
        consentRequestController.withConsumerToken(MPConsumers.OLP_CONS_1);
        var addVINsResponse = consentRequestController.addVinsToConsentRequest(
                crid, new VinsToFile(vins).csv()
        );
        StatusCodeExpects.expectOKStatusCode(addVINsResponse);
    }

    public ConsentInfo createConsentRequestFor(ProviderApplications providerApplication) {
        return createConsentRequestFor(
                providerApplication.provider.getName(),
                providerApplication.consumer.getConsumerName(),
                providerApplication.consumer.getRealm(),
                providerApplication.container.id
        );
    }

    @Step("Create consent request for provider: '{providerId}', consumer: '{consumerRealm}', container: '{containerId}'")
    public ConsentInfo createConsentRequestFor(String providerId, String consumerName, String consumerRealm,
            String containerId) {
        var targetConsentRequest = new ConsentRequestData()
                .providerId(providerId)
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
        StatusCodeExpects.expectCREATEDStatusCode(consentRequestResponse);

        var consentRequestId = consentRequestResponse.as(ConsentRequestIdResponse.class).getConsentRequestId();
        return new ConsentRequestToConsentInfo(consentRequestId, targetConsentRequest)
                .consentInfo()
                .consumerName(consumerName)
                .privacyPolicy(targetConsentRequest.getPrivacyPolicy())
                .additionalLinks(targetConsentRequest.getAdditionalLinks());
    }

}
