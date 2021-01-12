package com.here.platform.cm.consentStatus.revoke;


import com.here.platform.cm.consentStatus.BaseConsentStatusTests;
import com.here.platform.cm.controllers.ConsentStatusController.NewConsent;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.rest.model.ErrorResponse;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.RevokeConsent;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.dataProviders.reference.controllers.ReferenceTokenController;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.authentication.AuthController;
import java.util.Arrays;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Revoke consent")
@RevokeConsent
class RevokeConsentTests extends BaseConsentStatusTests {

    @Test
    @Tag("fabric_test")
    @DisplayName("Verify revoke of ConsentRequest")
    void revokeConsentRequestPositiveTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);

        DataSubject dataSubject = DataSubjects.getNextVinLength(provider.getVinLength()).getDataSubject();
        UserAccountSteps.removeVINFromDataSubject(dataSubject);
        UserAccountSteps.attachVINToUserAccount(dataSubject, dataSubject.getVin());
        String testVin = dataSubject.getVin();

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        var step = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin);
        var crid = step.getId();

        final var consentToRevoke = NewConsent.builder()
                .consentRequestId(crid)
                .vinHash(new VIN(testVin).hashed())
                .build();

        var revokedConsentResponse = consentStatusController
                .withConsumerToken()
                .revokeConsent(consentToRevoke, AuthController.getDataSubjectToken(dataSubject));

        new ResponseAssertion(revokedConsentResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEmpty();

        final var expectedStatusesForConsent = new ConsentRequestStatus()
                .approved(0)
                .pending(0)
                .revoked(1)
                .expired(0)
                .rejected(0);
        step.verifyConsentStatus(expectedStatusesForConsent);

        var revokedConsents = new UserAccountController()
                .getConsentsForUser(
                        AuthController.getDataSubjectToken(dataSubject),
                        Map.of("consentRequestId", crid, "state", "REVOKED")
                );

        var consentInfoList = new ResponseAssertion(revokedConsents)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAsListOf(ConsentInfo[].class);

        Assertions.assertThat(consentInfoList)
                .usingElementComparatorIgnoringFields("createTime", "revokeTime", "approveTime", "vinHash")
                .contains(
                        new ConsentInfo()
                                .additionalLinks(consentObj.getConsent().getAdditionalLinks())
                                .consentRequestId(crid)
                                .state(StateEnum.REVOKED)
                                .consumerName(mpConsumer.getName())
                                .consumerId(mpConsumer.getRealm())
                                .vinLabel(new VIN(testVin).label())
                                .title(consentObj.getConsent().getTitle())
                                .purpose(consentObj.getConsent().getPurpose())
                                .privacyPolicy(consentObj.getConsent().getPrivacyPolicy())
                                .containerName(targetContainer.getName())
                                .containerId(targetContainer.getId())
                                .containerDescription(targetContainer.getContainerDescription())
                                .resources(targetContainer.getResources())
                );

        var targetRevokedConsent = Arrays.stream(consentInfoList)
                .filter(consents -> consents.getConsentRequestId().equals(crid))
                .findFirst();

        Assertions.assertThat(targetRevokedConsent.orElseThrow().getCreateTime())
                .isBefore(targetRevokedConsent.orElseThrow().getRevokeTime());

    }

    @Test
    @DisplayName("Verify it is not possible to revoke consent that does not exist")
    void isNotPossibleToRevokeConsentThatDoesNotExitTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        DataSubjects dataSubject = DataSubjects.getNextVinLength(provider.getVinLength());
        String testVin = dataSubject.getVin();


        var randomConsentRequestId = crypto.sha256();
        var consentToRevoke = NewConsent.builder()
                .consentRequestId(randomConsentRequestId)
                .vinHash(new VIN(testVin).hashed())
                .build();

        var approveResponse = consentStatusController
                .withConsumerToken()
                .revokeConsent(consentToRevoke, dataSubject.getBearerToken());
        new ResponseAssertion(approveResponse).statusCodeIsEqualTo(StatusCode.NOT_FOUND);

        final var actualStatusesForConsent = consentRequestController
                .withConsumerToken()
                .getStatusForConsentRequestById(randomConsentRequestId);

        final var expectedStatuses = new ConsentRequestStatus()
                .approved(0)
                .pending(0)
                .revoked(0)
                .expired(0)
                .rejected(0);
        new ResponseAssertion(actualStatusesForConsent).responseIsEqualToObject(expectedStatuses);
    }

    @Test
    @DisplayName("Verify it is possible to approve similar to revoked consent")
    void approveSimilarToRevokedConsentsTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);

        DataSubjects dataSubject = DataSubjects.getNextVinLength(provider.getVinLength());
        String testVin = dataSubject.getVin();

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        var step = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin);
        var crid = step.getId();


        ConsentFlowSteps.approveConsentForVIN(crid, targetContainer, testVin);
        ConsentFlowSteps.revokeConsentForVIN(crid, testVin);

        ConsentObject consentObjNext = new ConsentObject(mpConsumer, provider, targetContainer);
        var consentRequestResponse = consentRequestController
                .withConsumerToken()
                .createConsentRequest(consentObjNext.getConsentRequestData());
        new ResponseAssertion(consentRequestResponse).statusCodeIsEqualTo(StatusCode.CONFLICT);
    }

    @Test
    @DisplayName("Verify it is not possible to approve revoked consent")
    void isNotPossibleToApproveRevokedConsentTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);

        DataSubjects dataSubject = DataSubjects.getNextVinLength(provider.getVinLength());
        String testVin = dataSubject.getVin();

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        var step = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin);
        var crid = step.getId();

        ConsentFlowSteps.approveConsentForVIN(crid, targetContainer, testVin);

        var validCode = ReferenceTokenController
                .produceConsentAuthCode(testVin, targetContainer.getId() + ":general");

        var consentUnderTest = NewConsent.builder()
                .consentRequestId(crid)
                .vinHash(new VIN(testVin).hashed())
                .authorizationCode(validCode)
                .build();

        consentStatusController
                .withConsumerToken()
                .revokeConsent(consentUnderTest, dataSubject.getBearerToken());

        var approveResponse = consentStatusController
                .withConsumerToken()
                .approveConsent(consentUnderTest, dataSubject.getBearerToken());
        ErrorResponse errorResponse = new ResponseAssertion(approveResponse)
                .statusCodeIsEqualTo(StatusCode.FORBIDDEN)
                .expectedErrorResponse(CMErrorResponse.CONSENT_ALREADY_REVOKED);

        Assertions.assertThat(errorResponse.getCause()).isEqualTo("Consent already revoked");
    }

}
