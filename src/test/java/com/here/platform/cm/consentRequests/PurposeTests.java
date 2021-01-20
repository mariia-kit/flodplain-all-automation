package com.here.platform.cm.consentRequests;


import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.ConsentRequestPurposeData;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.Purpose;
import com.here.platform.common.annotations.ErrorHandler;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Purpose
@DisplayName("Purpose for consent request")
@Tag("CM-Consent")
public class PurposeTests extends BaseCMTest {

    @Test
    @ErrorHandler
    @DisplayName("Get consent request purpose Not found")
    void purposeNotFoundForConsentRequestTest() {
        var privateBearer = DataSubjects.getNextBy18VINLength().getBearerToken();
        var purposeResponse = consentRequestController.getConsentRequestPurpose(crypto.sha1(), privateBearer);

        new ResponseAssertion(purposeResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.CONSENT_REQUEST_NOT_FOUND);
    }

    @Test
    @ErrorHandler
    @DisplayName("Forbidden to get purpose without Bearer token")
    void forbiddenToGetPurposeWithoutBearerTokenTest() {
        var purposeResponse = consentRequestController.getConsentRequestPurpose(crypto.sha1(), "");

        new ResponseAssertion(purposeResponse)
                .statusCodeIsEqualTo(StatusCode.UNAUTHORIZED)
                .expectedErrorResponse(CMErrorResponse.AUTHORIZATION_FAILED);
    }

    @Test
    @DisplayName("Verify purpose content of consent request")
    void getPurposeForConsentRequestTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);
        DataSubjects dataSubject = DataSubjects.getNextVinLength(provider.getVinLength());
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(dataSubject.getVin())
                .getId();

        var purposeResponse = consentRequestController
                .withConsumerToken()
                .getConsentRequestPurpose(crid, dataSubject.getBearerToken());

        new ResponseAssertion(purposeResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new ConsentRequestPurposeData()
                        .containerName(targetContainer.getName())
                        .containerDescription(targetContainer.getContainerDescription())
                        .resources(targetContainer.getResources())
                        .purpose(consentObj.getConsent().getPurpose())
                        .privacyPolicy(consentObj.getConsent().getPrivacyPolicy())
                        .consumerName(consentObj.getConsent().getConsumerName())
                        .title(consentObj.getConsent().getTitle())
                );
    }

    @Test
    @DisplayName("Get purpose data by consumerId and containerId")
    void getPurposeByConsumerAndContainerIdsTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);
        DataSubjects dataSubject = DataSubjects.getNextVinLength(provider.getVinLength());
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(dataSubject.getVin())
                .getId();

        var purposeResponse = consentRequestController
                .withConsumerToken()
                .getConsentRequestPurpose(
                        consentObj.getConsentRequestData().getConsumerId(),
                        consentObj.getConsentRequestData().getContainerId(),
                        dataSubject.getBearerToken()
                );

        new ResponseAssertion(purposeResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new ConsentRequestPurposeData()
                        .containerName(targetContainer.getName())
                        .containerDescription(targetContainer.getContainerDescription())
                        .resources(targetContainer.getResources())
                        .purpose(consentObj.getConsent().getPurpose())
                        .privacyPolicy(consentObj.getConsent().getPrivacyPolicy())
                        .consumerName(consentObj.getConsent().getConsumerName())
                        .title(consentObj.getConsent().getTitle())
                );
    }

}
