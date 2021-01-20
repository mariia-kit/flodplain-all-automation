package com.here.platform.cm.consentRequests;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.ConsentRequest;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.CreateConsentRequest;
import com.here.platform.common.strings.VIN;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Create consent request")
@CreateConsentRequest
@Tag("smoke_cm")
public class CreateConsentRequestsTests extends BaseCMTest {

    @Test
    @DisplayName("Success flow of consent request creation and getting consent request by ID")
    @Tag("fabric_test")
    void createAndGetConsentRequest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);

        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .getId();

        ConsentRequest expectedResponse = consentObj.generateResponseForCreation();

        var consentRequestByIdResponse = consentRequestController
                .withConsumerToken()
                .getConsentRequestById(crid);
        new ResponseAssertion(consentRequestByIdResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedResponse);
    }

    @Test
    @DisplayName("Verify Adding Vins To Empty ConsentRequest Via File")
    @Tag("fabric_test")
    void addVinsToEmptyConsentRequestViaFileTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);
        var testVin = VIN.generate(provider.getVinLength());
        var testVin1 = VIN.generate(provider.getVinLength());

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);

        new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin, testVin1)
                .verifyConsentStatus(
                        new ConsentRequestStatus()
                        .pending(2)
                        .approved(0).revoked(0).expired(0).rejected(0));
    }

    @Issue("NS-3043")
    @Test
    @DisplayName("Is not possible to create consent request without privacy policy and additional links")
    void isPossibleToCreateConsentReqeustWithoutPrivacyPolicyAndAdditionalLinks() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        consentObj.getConsentRequestData()
                .privacyPolicy(null)
                .additionalLinks(null);
        new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest();

        var consentRequestResponse = consentRequestController
                .withConsumerToken()
                .createConsentRequest(consentObj.getConsentRequestData());

        new ResponseAssertion(consentRequestResponse)
                .statusCodeIsEqualTo(StatusCode.BAD_REQUEST)
                .expectedErrorResponse(CMErrorResponse.PARAMETER_VALIDATION);
        new ResponseAssertion(consentRequestResponse)
                .expectedErrorCause("Property 'consentRequestData.privacyPolicy' must not be blank");
    }

}
