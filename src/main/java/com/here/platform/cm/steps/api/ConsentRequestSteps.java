package com.here.platform.cm.steps.api;

import static com.here.platform.common.strings.SBB.sbb;

import com.github.javafaker.Faker;
import com.here.platform.cm.controllers.ConsentRequestController;
import com.here.platform.cm.dataAdapters.ConsentRequestToConsentInfo;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.Consents;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.AdditionalLink;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.VinsToFile.FILE_TYPE;
import com.here.platform.common.config.Conf;
import com.here.platform.common.strings.VIN;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.Steps;
import io.qameta.allure.Step;
import java.util.List;
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
                container.getClientSecret()
        );
        return ConsentRequestSteps.createConsentRequestWithVINFor(
                targetApp.provider.getName(),
                targetApp.consumer.getName(),
                targetApp.consumer.getRealm(),
                container.getId(),
                testVin);
    }

    @Step("Add VINs: '{vins}', to consent request: '{crid}'")
    public void addVINsToConsentRequest(ProviderApplications providerApplication, String crid, String... vins) {
        consentRequestController.withAuthorizationValue(providerApplication.consumer.getToken());
        var addVINsResponse = consentRequestController.addVinsToConsentRequest(
                crid, FILE_TYPE.JSON, vins);
        StatusCodeExpects.expectOKStatusCode(addVINsResponse);
    }

    @Step("Add VINs: '{vins}', to consent request: '{crid}'")
    public void addVINsToConsentRequestAsConsumer(String crid, String... vins) {
        consentRequestController.withAuthorizationValue(Users.MP_CONSUMER.getToken());
        var addVINsResponse = consentRequestController.addVinsToConsentRequest(
                crid, FILE_TYPE.JSON, vins);
        StatusCodeExpects.expectOKStatusCode(addVINsResponse);
    }

    @Step("Create consent request for provider: '{providerId}', consumer: '{consumerRealm}', container: '{containerId}'")
    public ConsentInfo createConsentRequestFor(String providerId, String consumerName, String consumerRealm,
            String containerId) {
        var targetConsentRequest = Consents.generateNewConsent(consumerRealm, providerId, containerId);

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

    @Step("Create consent request for provider: '{providerId}', consumer: '{consentInfo.consumerId}', container: '{consentInfo.containerId}'")
    public ConsentInfo createConsentRequest(String providerId, ConsentInfo consentInfo) {
        var targetConsentRequest = Consents.generateNewConsent(providerId, consentInfo);

        consentRequestController.withConsumerToken();
        var consentRequestResponse = consentRequestController.createConsentRequest(targetConsentRequest);
        StatusCodeExpects.expectCREATEDStatusCode(consentRequestResponse);

        var consentRequestId = consentRequestResponse.as(ConsentRequestIdResponse.class).getConsentRequestId();
        consentInfo.setConsentRequestId(consentRequestId);
        return consentInfo;
    }

    @Step("Onboard provider {providerId} with containers on NS and CM {consentInfo.containerId} for {consentInfo.consumerId}")
    public ConsentInfo onboardAllForConsentRequest(String providerId, ConsentInfo consentInfo) {
        ConsentRequestContainer consentRequestContainer = ConsentRequestContainer.builder()
                .id(consentInfo.getContainerId())
                .name(consentInfo.getContainerName())
                .scopeValue("mb:user:pool:reader mb:vehicle:status:general offline_access")
                .resources(consentInfo.getResources())
                .containerDescription(consentInfo.getContainerDescription())
                .provider(MPProviders.findByProviderId(providerId))
                .clientId(Conf.ns().getReferenceApp().getClientId())
                .clientSecret(Conf.ns().getReferenceApp().getClientSecret())
                .build();
        Steps.createRegularContainer(consentRequestContainer);
        OnboardingSteps onboard = new OnboardingSteps(providerId, consentInfo.getConsumerId());
        onboard.onboardTestProvider();
        onboard.onboardValidConsumer();
        onboard.onboardTestProviderApplication(
                consentRequestContainer.getId(),
                consentRequestContainer.getClientId(),
                consentRequestContainer.getClientSecret()
        );
        return consentInfo;
    }

    @Step("Add VINs: '{vins}', to consent request: '{crid}'")
    public void addVINsToConsentRequest(String crid, String... vins) {
        consentRequestController.withAuthorizationValue(Users.MP_CONSUMER.getToken());
        var addVINsResponse = consentRequestController.addVinsToConsentRequest(
                crid, new VinsToFile(vins).csv()
        );
        StatusCodeExpects.expectOKStatusCode(addVINsResponse);
    }
}
