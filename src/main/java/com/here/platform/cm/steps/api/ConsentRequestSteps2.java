package com.here.platform.cm.steps.api;

import com.here.platform.cm.controllers.ConsentRequestController;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.Consents;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.rest.model.ConsentStatus;
import com.here.platform.cm.rest.model.ProviderApplication;
import com.here.platform.cm.steps.remove.ConsentCollector;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VinsToFile.FILE_TYPE;
import com.here.platform.common.config.Conf;
import com.here.platform.common.strings.VIN;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.Steps;
import io.qameta.allure.Step;


public class ConsentRequestSteps2 {

    private final ConsentInfo consentInfo;
    private final ConsentRequestContainer consentRequestContainer;

    private final ConsentRequestController consentRequestController = new ConsentRequestController();

    public ConsentRequestSteps2(ConsentRequestContainer consentRequestContainer, ConsentInfo consentInfo) {
        this.consentInfo = consentInfo;
        this.consentRequestContainer = consentRequestContainer;
    }

    @Step("Create regular consent request.")
    public ConsentRequestSteps2 createConsentRequest() {
        var targetConsentRequest = Consents.generateNewConsent(
                consentRequestContainer.getProvider().getName(),
                consentInfo);

        var consentRequestResponse = consentRequestController
                .withConsumerToken()
                .createConsentRequest(targetConsentRequest);
        StatusCodeExpects.expectCREATEDStatusCode(consentRequestResponse);

        var consentRequestId = consentRequestResponse.as(ConsentRequestIdResponse.class).getConsentRequestId();
        consentInfo.setConsentRequestId(consentRequestId);
        return this;
    }

    @Step("Onboard provider with containers on NS and CM.")
    public ConsentRequestSteps2 onboardAllForConsentRequest() {
        Steps.createRegularContainer(consentRequestContainer);
        OnboardingSteps onboard = new OnboardingSteps(
                MPProviders.findByProviderId(consentRequestContainer.getProvider().getName()),
                consentInfo.getConsumerId());
        ConsentCollector.addApp(new ProviderApplication()
                .providerId(consentRequestContainer.getProvider().getName())
                .consumerId(consentInfo.getConsumerId())
                .containerId(consentRequestContainer.getId()));
        onboard.onboardTestProvider();
        onboard.onboardConsumer(consentInfo.getConsumerName());
        onboard.onboardTestProviderApplication(
                consentRequestContainer.getId(),
                consentRequestContainer.getClientId(),
                consentRequestContainer.getClientSecret()
        );
        return this;
    }

    @Step("Add VINs: '{vins}' to consent request.")
    public ConsentRequestSteps2 addVINsToConsentRequest(String... vins) {
        var addVINsResponse = consentRequestController
                .withAuthorizationValue(Users.MP_CONSUMER.getToken())
                .addVinsToConsentRequest(consentInfo.getConsentRequestId(), FILE_TYPE.JSON, vins);
        StatusCodeExpects.expectOKStatusCode(addVINsResponse);
        return this;
    }
    @Step("Remove VINs: '{vins}' from consent request.")
    public ConsentRequestSteps2 removeVINsFromConsentRequest(String... vins) {
        var addVINsResponse = consentRequestController
                .withConsumerToken()
                .removeVinsFromConsentRequest(consentInfo.getConsentRequestId(), FILE_TYPE.JSON, vins);
        StatusCodeExpects.expectOKStatusCode(addVINsResponse);
        return this;
    }

    @Step("Verify current consent status {expectedConsentRequestStatuses}")
    public ConsentRequestSteps2 verifyConsentStatus(ConsentRequestStatus expectedConsentRequestStatuses) {
        var statusForConsentRequestByIdResponse = consentRequestController
                .withConsumerToken()
                .getStatusForConsentRequestById(consentInfo.getConsentRequestId());
        new ResponseAssertion(statusForConsentRequestByIdResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedConsentRequestStatuses);
        return this;
    }

    @Step("Verify current consent status {expectedConsentRequestStatuses}")
    public ConsentRequestSteps2 verifyConsentStatusByVin(String vin, String status) {
        ConsentStatus consentStatus = new ConsentStatus()
                .consentRequestId(consentInfo.getConsentRequestId())
                .vin(vin)
                .state(status);
        var statusForConsentRequestByVinResponse = new ConsentStatusController()
                .withConsumerToken()
                .getConsentStatusByIdAndVin(consentInfo.getConsentRequestId(), vin);
        new ResponseAssertion(statusForConsentRequestByVinResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(consentStatus);
        return this;
    }


    public String getId() {
        return consentInfo.getConsentRequestId();
    }

}
