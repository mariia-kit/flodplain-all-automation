package com.here.platform.cm.consentStatus;


import com.here.platform.dataProviders.reference.ReferenceTokenController;
import com.here.platform.cm.controllers.ConsentStatusController.NewConsent;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.rest.model.ErrorResponse;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.annotations.CMFeatures.RevokeConsent;
import com.here.platform.common.config.Conf;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


@DisplayName("Revoke consent")
@RevokeConsent
class RevokeConsentTests extends BaseConsentStatusTests {

    private String crid;

    @AfterEach
    void cleanUp() {
        if (Objects.nonNull(crid)) {
            RemoveEntitiesSteps.cascadeForceRemoveConsentRequest(crid, testFileWithVINs, testConsentRequestData);
        }
    }

    @Test
    @DisplayName("Verify revoke of ConsentRequest")
    @Disabled("Bug fix required NS-2805")
    void revokeConsentRequestPositiveTest() {
        new UserAccountController().attachVinToUserAccount(testVin, dataSubject.getBearerToken());

        crid = createValidConsentRequest();

        final var consentToRevoke = NewConsent.builder()
                .consentRequestId(crid)
                .vinHash(new VIN(testVin).hashed())
                .build();

        fuSleep();
        var privateBearer = dataSubject.getBearerToken();
        var revokedConsentResponse = consentStatusController.revokeConsent(consentToRevoke, privateBearer);

        new ResponseAssertion(revokedConsentResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEmpty();

        final var expectedStatusesForConsent = new ConsentRequestStatus()
                .approved(0)
                .pending(0)
                .revoked(1)
                .expired(0)
                .rejected(0);
        consentRequestController.withConsumerToken();
        final var actualStatusesForConsentResponse = consentRequestController
                .getStatusForConsentRequestById(consentToRevoke.getConsentRequestId());

        new ResponseAssertion(actualStatusesForConsentResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedStatusesForConsent);

        var revokedConsents = new UserAccountController()
                .getConsentsForUser(
                        privateBearer,
                        Map.of("consentRequestId", crid, "state", "REVOKED")
                );

        var consentInfoList = new ResponseAssertion(revokedConsents)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAsListOf(ConsentInfo[].class);

        Assertions.assertThat(consentInfoList)
                .usingElementComparatorIgnoringFields("createTime", "revokeTime", "approveTime", "vinHash")
                .contains(
                        new ConsentInfo()
                                .additionalLinks(testConsentRequestData.getAdditionalLinks())
                                .consentRequestId(crid)
                                .state(StateEnum.REVOKED)
                                .consumerName(mpConsumer.getConsumerName())
                                .vinLabel(new VIN(testVin).label())
                                .title(testConsentRequestData.getTitle())
                                .purpose(testConsentRequestData.getPurpose())
                                .privacyPolicy(testConsentRequestData.getPrivacyPolicy())
                                .containerName(testContainer.name)
                                .containerDescription(testContainer.containerDescription)
                                .resources(testContainer.resources)
                );

        var targetRevokedConsent = Arrays.stream(consentInfoList)
                .filter(consentInfo -> consentInfo.getConsentRequestId().equals(crid))
                .findFirst();

        Assertions.assertThat(targetRevokedConsent.orElseThrow().getCreateTime())
                .isBefore(targetRevokedConsent.orElseThrow().getRevokeTime());

    }

    @Test
    @DisplayName("Verify it is not possible to revoke consent that does not exist")
    void isNotPossibleToRevokeConsentThatDoesNotExitTest() {
        var randomConsentRequestId = crypto.sha256();
        var consentToRevoke = NewConsent.builder()
                .consentRequestId(randomConsentRequestId)
                .vinHash(new VIN(testVin).hashed())
                .build();

        var privateBearer = dataSubject.getBearerToken();
        var approveResponse = consentStatusController.revokeConsent(consentToRevoke, privateBearer);
        new ResponseAssertion(approveResponse).statusCodeIsEqualTo(StatusCode.NOT_FOUND);

        consentRequestController.withConsumerToken();
        final var actualStatusesForConsent = consentRequestController
                .getStatusForConsentRequestById(randomConsentRequestId);

        final var expectedStatuses = new ConsentRequestStatus()
                .approved(0)
                .pending(0)
                .revoked(0)
                .expired(0)
                .rejected(0);
        new ResponseAssertion(actualStatusesForConsent).responseIsEqualToObject(expectedStatuses);
    }

    @Nested
    @RevokeConsent
    @DisplayName("Revoke approved consents")
    public class RevokeApprovedTests {

        private String validDaimlerToken, privateBearer;

        @BeforeEach
        void generateDaimlerAuthorisationTokenForTestCar() {
            privateBearer = dataSubject.getBearerToken();
            validDaimlerToken = ReferenceTokenController
                    .produceConsentAuthCode(testVin, testContainer.getId() + ":general");
        }

        @Test
        @DisplayName("Verify it is possible to approve similar to revoked consent")
        void approveSimilarToRevokedConsentsTest() {
            testConsentRequestData
                    .containerId(testContainer.id)
                    .title(Conf.cm().getQaTestDataMarker() + faker.gameOfThrones().quote())
                    .privacyPolicy(faker.internet().url())
                    .purpose(faker.commerce().productName());

            var crid = createValidConsentRequest();

            var consentUnderTest = NewConsent.builder()
                    .consentRequestId(crid)
                    .vinHash(new VIN(testVin).hashed())
                    .build();

            var revokedConsentResponse = consentStatusController.revokeConsent(consentUnderTest, privateBearer);
            new ResponseAssertion(revokedConsentResponse)
                    .statusCodeIsEqualTo(StatusCode.OK)
                    .responseIsEmpty();

            consentRequestController.withConsumerToken();
            var consentRequestResponse = consentRequestController.createConsentRequest(testConsentRequestData);
            new ResponseAssertion(consentRequestResponse).statusCodeIsEqualTo(StatusCode.CONFLICT);
        }

        @Test
        @DisplayName("Verify it is not possible to approve revoked consent")
        void isNotPossibleToApproveRevokedConsentTest() {
            crid = createValidConsentRequest();

            var consentUnderTest = NewConsent.builder()
                    .consentRequestId(crid)
                    .vinHash(new VIN(testVin).hashed())
                    .authorizationCode(validDaimlerToken)
                    .build();

            fuSleep();
            consentStatusController.revokeConsent(consentUnderTest, privateBearer);
            fuSleep();
            var approveResponse = consentStatusController.approveConsent(consentUnderTest, privateBearer);
            ErrorResponse errorResponse = new ResponseAssertion(approveResponse)
                    .statusCodeIsEqualTo(StatusCode.FORBIDDEN)
                    .expectedErrorResponse(CMErrorResponse.CONSENT_ALREADY_REVOKED);

            Assertions.assertThat(errorResponse.getCause()).isEqualTo("Consent already revoked");
        }

    }

}
