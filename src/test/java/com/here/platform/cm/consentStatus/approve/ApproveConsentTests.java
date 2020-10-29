package com.here.platform.cm.consentStatus.approve;


import com.here.platform.dataProviders.reference.ReferenceTokenController;
import com.here.platform.cm.consentStatus.BaseConsentStatusTests;
import com.here.platform.cm.controllers.ConsentStatusController.NewConsent;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.rest.model.SuccessApproveData;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.annotations.CMFeatures.ApproveConsent;
import com.here.platform.common.annotations.ErrorHandler;
import com.here.platform.common.annotations.Sentry;
import java.util.Objects;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Approve consent")
@ApproveConsent
class ApproveConsentTests extends BaseConsentStatusTests {

    private final String privateBearer = dataSubject.getBearerToken();
    private String crid;


    @AfterEach
    void cleanUp() {
        if (Objects.nonNull(crid)) {
            RemoveEntitiesSteps.cascadeForceRemoveConsentRequest(crid, testFileWithVINs, testConsentRequestData);
        }
    }

    @Test
    @DisplayName("Verify Approve Consent GetStatus")
    @Tag("cm_prod")
    void createApproveGetConsentStatusTest() {
        crid = createValidConsentRequest();

        var validCode = ReferenceTokenController
                .produceConsentAuthCode(testVin, testContainer.getId() + ":general");

        final var consentToApprove = NewConsent.builder()
                .vinHash(new VIN(testVin).hashed())
                .consentRequestId(crid)
                .authorizationCode(validCode)
                .build();

        var approveConsentResponse = consentStatusController.approveConsent(consentToApprove, privateBearer);

        var successApproveData = new ResponseAssertion(approveConsentResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(SuccessApproveData.class);

        var expectedApprovedConsentInfo = new ConsentInfo()
                .consentRequestId(crid)
                .purpose(testConsentRequestData.getPurpose())
                .title(testConsentRequestData.getTitle())
                .consumerName(mpConsumer.getConsumerName())
                .state(StateEnum.APPROVED)
                .revokeTime(null)
                .containerName(testContainer.getName())
                .containerDescription(testContainer.getContainerDescription())
                .resources(testContainer.getResources())
                .additionalLinks(testConsentRequestData.getAdditionalLinks())
                .privacyPolicy(testConsentRequestData.getPrivacyPolicy())
                .vinLabel(new VIN(testVin).label());

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
        consentRequestController.withConsumerToken();
        final var actualStatusesForConsent = consentRequestController
                .getStatusForConsentRequestById(consentToApprove.getConsentRequestId());

        new ResponseAssertion(actualStatusesForConsent).responseIsEqualToObject(expectedStatusesForConsent);
    }

    @Test
    @DisplayName("Verify It Is Not Possible To Approve Consent That Does Not Exist")
    void isNotPossibleToApproveConsentThatDoesNotExistTest() {
        var randomConsentRequestId = crypto.sha256();
        final var consentToApprove = NewConsent.builder()
                .vinHash(new VIN(testVin).hashed())
                .consentRequestId(randomConsentRequestId)
                .authorizationCode(crypto.sha1())
                .build();

        var approveResponse = consentStatusController.approveConsent(consentToApprove, privateBearer);
        new ResponseAssertion(approveResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.CONSENT_REQUEST_NOT_FOUND);

        consentRequestController.withConsumerToken();
        final var actualStatusesForConsent = consentRequestController
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
    @DisplayName("Verify Approve with Empty Consent for ConsentRequest")
    void approveConsentErrorHandlerTest() {
        var approveResponse = consentStatusController.approveConsent(NewConsent.builder().build(), privateBearer);

        new ResponseAssertion(approveResponse)
                .statusCodeIsEqualTo(StatusCode.BAD_REQUEST)
                .expectedErrorResponse(CMErrorResponse.CONSENT_VALIDATION);
    }

    @Test
    @Sentry
    @DisplayName("Verify Sentry Block Approve ConsentRequest")
    void sentryBlockApproveConsentRequestTest() {
        crid = createValidConsentRequest();

        final var consentToApprove = NewConsent.builder()
                .vinHash(new VIN(testVin).hashed())
                .consentRequestId(crid)
                .authorizationCode(crypto.sha1())
                .build();

        var approveResponse = consentStatusController.approveConsent(consentToApprove, "");
        new ResponseAssertion(approveResponse).statusCodeIsEqualTo(StatusCode.UNAUTHORIZED);
    }

}
