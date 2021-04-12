package com.here.platform.cm.consentStatus.approve;


import com.here.platform.cm.consentStatus.BaseConsentStatusTests;
import com.here.platform.cm.controllers.ConsentStatusController.NewConsent;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.rest.model.SuccessApproveData;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.ApproveConsent;
import com.here.platform.common.annotations.ErrorHandler;
import com.here.platform.common.annotations.Sentry;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.dataProviders.reference.controllers.ReferenceTokenController;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Approve consent")
@Disabled
@ApproveConsent
class ApproveConsentTests extends BaseConsentStatusTests {

    @Test
    @DisplayName("Verify Approve Consent GetStatus")
    @Tag("cm_prod")
    @Tag("fabric_test")
    void createApproveGetConsentStatusTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);

        DataSubjects dataSubject = DataSubjects.getNextVinLength(provider.getVinLength());
        String vinToApprove = dataSubject.getVin();

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vinToApprove)
                .getId();

        var validCode = ReferenceTokenController
                .produceConsentAuthCode(vinToApprove, targetContainer.getId() + ":general");

        final var consentToApprove = NewConsent.builder()
                .vinHash(new VIN(vinToApprove).hashed())
                .consentRequestId(crid)
                .authorizationCode(validCode)
                .build();

        var approveConsentResponse = consentStatusController
                .withConsumerToken()
                .approveConsent(consentToApprove, dataSubject.getBearerToken());

        var successApproveData = new ResponseAssertion(approveConsentResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(SuccessApproveData.class);

        var expectedApprovedConsentInfo = new ConsentInfo()
                .consentRequestId(crid)
                .purpose(consentObj.getConsent().getPurpose())
                .title(consentObj.getConsent().getTitle())
                .consumerName(mpConsumer.getName())
                .consumerId(mpConsumer.getRealm())
                .state(StateEnum.APPROVED)
                .revokeTime(null)
                .containerName(targetContainer.getName())
                .containerId(targetContainer.getId())
                .containerDescription(targetContainer.getContainerDescription())
                .resources(targetContainer.getResources())
                .additionalLinks(consentObj.getConsent().getAdditionalLinks())
                .privacyPolicy(consentObj.getConsent().getPrivacyPolicy())
                .vinLabel(new VIN(vinToApprove).label());

        Assertions.assertThat(successApproveData.getApprovedConsentInfo())
                .isEqualToIgnoringGivenFields(expectedApprovedConsentInfo, ResponseAssertion.timeFieldsToIgnore);
        Assertions.assertThat(successApproveData.getApprovedConsentInfo().getApproveTime())
                .isAfter(successApproveData.getApprovedConsentInfo().getCreateTime());

        final var expectedStatusesForConsent = new ConsentRequestStatus()
                .approved(1)
                .pending(0)
                .revoked(0)
                .expired(0)
                .rejected(0);
        final var actualStatusesForConsent = consentRequestController
                .withConsumerToken()
                .getStatusForConsentRequestById(consentToApprove.getConsentRequestId());

        new ResponseAssertion(actualStatusesForConsent).responseIsEqualToObject(expectedStatusesForConsent);
    }

    @Test
    @DisplayName("Verify it is not possible to approve absent consent")
    void isNotPossibleToApproveConsentThatDoesNotExistTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        DataSubjects dataSubject = DataSubjects.getNextVinLength(provider.getVinLength());
        String vinToApprove = dataSubject.getVin();

        var randomConsentRequestId = crypto.sha256();
        final var consentToApprove = NewConsent.builder()
                .vinHash(new VIN(vinToApprove).hashed())
                .consentRequestId(randomConsentRequestId)
                .authorizationCode(crypto.sha1())
                .build();

        var approveResponse = consentStatusController
                .withConsumerToken()
                .approveConsent(consentToApprove, dataSubject.getBearerToken());
        new ResponseAssertion(approveResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.CONSENT_REQUEST_NOT_FOUND);

        final var actualStatusesForConsent = consentRequestController
                .withConsumerToken()
                .getStatusForConsentRequestById(randomConsentRequestId);

        final var expectedStatuses = new ConsentRequestStatus()
                .approved(0)
                .pending(0)
                .revoked(0)
                .expired(0)
                .rejected(0);

        new ResponseAssertion(actualStatusesForConsent)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedStatuses);
    }

    @Test
    @ErrorHandler
    @DisplayName("Verify approve consent with empty Consent body")
    void approveConsentErrorHandlerTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        DataSubjects dataSubject = DataSubjects.getNextVinLength(provider.getVinLength());

        var approveResponse = consentStatusController
                .withConsumerToken()
                .approveConsent(NewConsent.builder().build(), dataSubject.getBearerToken());

        new ResponseAssertion(approveResponse)
                .statusCodeIsEqualTo(StatusCode.BAD_REQUEST)
                .expectedErrorResponse(CMErrorResponse.CONSENT_VALIDATION);
    }

    @Test
    @Sentry
    @DisplayName("Verify sentry is blocking consent approval with empty CM application token")
    void sentryBlockApproveConsentRequestTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);

        DataSubjects dataSubject = DataSubjects.getNextVinLength(provider.getVinLength());
        String vinToApprove = dataSubject.getVin();

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vinToApprove)
                .getId();

        final var consentToApprove = NewConsent.builder()
                .vinHash(new VIN(vinToApprove).hashed())
                .consentRequestId(crid)
                .authorizationCode(crypto.sha1())
                .build();

        var approveResponse = consentStatusController
                .withConsumerToken()
                .approveConsent(consentToApprove, "");
        new ResponseAssertion(approveResponse).statusCodeIsEqualTo(StatusCode.UNAUTHORIZED);
    }

}
