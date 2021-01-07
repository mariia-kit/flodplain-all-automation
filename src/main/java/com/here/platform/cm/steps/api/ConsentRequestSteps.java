package com.here.platform.cm.steps.api;

import static io.qameta.allure.Allure.step;

import com.here.platform.cm.controllers.ConsentRequestController;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.rest.model.ConsentStatus;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VinsToFile.FILE_TYPE;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.Steps;
import io.qameta.allure.Step;
import java.util.Arrays;


public class ConsentRequestSteps {

    private final ConsentObject consentObject;

    private final ConsentRequestController consentRequestController = new ConsentRequestController();

    public ConsentRequestSteps(ConsentObject consentObject) {
        this.consentObject = consentObject;
    }

    public ConsentRequestSteps(User consumer, MPProviders provider, ConsentRequestContainer container) {
        this.consentObject = new ConsentObject(consumer, provider, container);
    }

    public ConsentRequestSteps createConsentRequest() {
        step(String.format("Create regular consent request for provider:%s consumer:%s and container:%s",
                consentObject.getProvider().getName(),
                consentObject.getConsumer().getRealm(),
                consentObject.getContainer().getId()), () -> {
                    var consentRequestResponse = consentRequestController
                            .withConsumerToken()
                            .createConsentRequest(consentObject.getConsentRequestData());
                    StatusCodeExpects.expectCREATEDStatusCode(consentRequestResponse);

                    var consentRequestId = consentRequestResponse
                            .as(ConsentRequestIdResponse.class)
                            .getConsentRequestId();
                    consentObject.setCrid(consentRequestId);
                }
        );
        return this;
    }

    public ConsentRequestSteps onboardAllForConsentRequest() {
        step(String.format("Onboard provider:%s consumer:%s with container:%s on NS and CM.",
                consentObject.getProvider().getName(),
                consentObject.getConsumer().getRealm(),
                consentObject.getContainer().getId()), () -> {

                    Steps.createRegularContainer(consentObject.getContainer());
                    OnboardingSteps onboard = new OnboardingSteps(
                            consentObject.getProvider(),
                            consentObject.getConsumer().getRealm());
                    onboard.onboardTestProvider();
                    onboard.onboardConsumer(consentObject.getConsumer().getName());
                    onboard.onboardTestProviderApplication(
                            consentObject.getContainer().getId(),
                            consentObject.getContainer().getClientId(),
                            consentObject.getContainer().getClientSecret()
                    );
                }
        );
        return this;
    }

    public ConsentRequestSteps onboardApplicationForConsentRequest() {
        step(String.format("Onboard provider:%s consumer:%s with container:%s on CM.",
                consentObject.getProvider().getName(),
                consentObject.getConsumer().getRealm(),
                consentObject.getContainer().getId()), () -> {

                    OnboardingSteps onboard = new OnboardingSteps(
                            consentObject.getProvider(),
                            consentObject.getConsumer().getRealm());
                    onboard.onboardTestProvider();
                    onboard.onboardConsumer(consentObject.getConsumer().getName());
                    onboard.onboardTestProviderApplication(
                            consentObject.getContainer().getId(),
                            consentObject.getContainer().getClientId(),
                            consentObject.getContainer().getClientSecret()
                    );
                }
        );
        return this;
    }

    @Step("Add VINs: '{vins}' to consent request.")
    public ConsentRequestSteps addVINsToConsentRequest(String... vins) {
        var addVINsResponse = consentRequestController
                .withAuthorizationValue(Users.MP_CONSUMER.getToken())
                .addVinsToConsentRequest(consentObject.getCrid(), FILE_TYPE.JSON, vins);
        StatusCodeExpects.expectOKStatusCode(addVINsResponse);
        Arrays.stream(vins).filter(vin -> consentObject.getConsent(vin) == null).forEach(consentObject::addVin);
        return this;
    }
    @Step("Remove VINs: '{vins}' from consent request.")
    public ConsentRequestSteps removeVINsFromConsentRequest(String... vins) {
        var addVINsResponse = consentRequestController
                .withConsumerToken()
                .removeVinsFromConsentRequest(consentObject.getCrid(), FILE_TYPE.JSON, vins);
        StatusCodeExpects.expectOKStatusCode(addVINsResponse);
        return this;
    }

    @Step("Verify current consent status {expectedConsentRequestStatuses}")
    public ConsentRequestSteps verifyConsentStatus(ConsentRequestStatus expectedConsentRequestStatuses) {
        var statusForConsentRequestByIdResponse = consentRequestController
                .withConsumerToken()
                .getStatusForConsentRequestById(consentObject.getCrid());
        new ResponseAssertion(statusForConsentRequestByIdResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedConsentRequestStatuses);
        return this;
    }

    @Step("Verify current consent status for {vin} is {status}")
    public ConsentRequestSteps verifyConsentStatusByVin(String vin, String status) {
        ConsentStatus consentStatus = new ConsentStatus()
                .consentRequestId(consentObject.getCrid())
                .vin(vin)
                .state(status);
        var statusForConsentRequestByVinResponse = new ConsentStatusController()
                .withConsumerToken()
                .getConsentStatusByIdAndVin(consentObject.getCrid(), vin);
        new ResponseAssertion(statusForConsentRequestByVinResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(consentStatus);
        return this;
    }

    public ConsentRequestSteps approveConsent(String vin) {
        ConsentFlowSteps.approveConsentForVIN(consentObject.getCrid(), consentObject.getContainer(), vin);
        return this;
    }


    public String getId() {
        return consentObject.getCrid();
    }

}
