package com.here.platform.cm.consentStatus;

import com.here.platform.aaa.HereCMBearerAuthorization;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.annotations.CMFeatures.RevokeConsent;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.controllers.ConsentStatusController.NewConsent;
import com.here.platform.cm.controllers.ConsentStatusController.PageableConsent;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.steps.RemoveEntitiesSteps;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.dataProviders.DaimlerTokenController;
import java.util.Arrays;
import java.util.Objects;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
    void revokeConsentRequestPositiveTest() {
        crid = createValidConsentRequest();

        final var consentToRevoke = NewConsent.builder()
                .consentRequestId(crid)
                .vinHash(new VIN(testVin).hashed())
                .build();

        fuSleep();
        var privateBearer = HereCMBearerAuthorization.getCmToken(vehicle);
        var revokedConsentResponse = consentStatusController.revokeConsent(consentToRevoke, privateBearer);

        new ResponseAssertion(revokedConsentResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEmpty();

        final var expectedStatusesForConsent = new ConsentRequestStatus().approved(0).pending(0).revoked(1);
        consentRequestController.withCMToken();
        final var actualStatusesForConsentResponse = consentRequestController
                .getStatusForConsentRequestById(consentToRevoke.getConsentRequestId());

        new ResponseAssertion(actualStatusesForConsentResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedStatusesForConsent);

        var revokedConsentInfo = new ConsentStatusController()
                .getConsentsInfo(testVin, PageableConsent.builder().stateEnum(StateEnum.REVOKED).build());

        var consentInfoList = new ResponseAssertion(revokedConsentInfo)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAsListOf(ConsentInfo[].class);

        Assertions.assertThat(consentInfoList)
                .usingElementComparatorIgnoringFields("createTime", "revokeTime", "approveTime", "vinHash")
                .contains(
                        new ConsentInfo()
                                .consentRequestId(crid)
                                .state(StateEnum.REVOKED)
                                .consumerName(mpConsumer.getConsumerName())
                                .vinLabel(new VIN(testVin).label())
                                .title(testConsentRequestData.getTitle())
                                .purpose(testConsentRequestData.getPurpose())
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

        var privateBearer = vehicle.getBearerToken();
        var approveResponse = consentStatusController.revokeConsent(consentToRevoke, privateBearer);
        new ResponseAssertion(approveResponse).statusCodeIsEqualTo(StatusCode.NOT_FOUND);

        consentRequestController.withCMToken();
        final var actualStatusesForConsent = consentRequestController
                .getStatusForConsentRequestById(randomConsentRequestId);

        final var expectedStatuses = new ConsentRequestStatus()
                .approved(0)
                .pending(0)
                .revoked(0);
        new ResponseAssertion(actualStatusesForConsent).responseIsEqualToObject(expectedStatuses);
    }

    @Nested
    @RevokeConsent
    @DisplayName("Revoke approved consents")
    public class RevokeApprovedTests {

        private String validDaimlerToken, privateBearer;

        @BeforeEach
        void generateDaimlerAuthorisationTokenForTestCar() {
            privateBearer = HereCMBearerAuthorization.getCmToken(vehicle);
            validDaimlerToken = new DaimlerTokenController(testVin, testContainer).generateAuthorizationCode();
        }

        @Test
        @DisplayName("Verify it is possible to approve similar to revoked consent")
        void approveSimilarToRevokedConsentsTest() {
            testConsentRequestData
                    .containerName(testContainer.id)
                    .title(faker.gameOfThrones().quote())
                    .privacyPolicy(faker.internet().url())
                    .purpose(faker.commerce().productName());

            var crid = createValidConsentRequest();
            consentRequestController.addVinsToConsentRequest(crid, new VinsToFile(testVin).json());

            var consentUnderTest = NewConsent.builder()
                    .consentRequestId(crid)
                    .vinHash(new VIN(testVin).hashed())
                    .build();

            var revokedConsentResponse = consentStatusController.revokeConsent(consentUnderTest, privateBearer);
            new ResponseAssertion(revokedConsentResponse)
                    .statusCodeIsEqualTo(StatusCode.OK)
                    .responseIsEmpty();

            var secondTestConsentRequestId = createValidConsentRequest();
            consentUnderTest.setConsentRequestId(secondTestConsentRequestId);
            consentUnderTest.setVinHash(new VIN(testVin).hashed());
            consentUnderTest.setAuthorizationCode(validDaimlerToken);

            fuSleep();
            var approveResponse = consentStatusController.approveConsent(consentUnderTest, privateBearer);
            new ResponseAssertion(approveResponse).statusCodeIsEqualTo(StatusCode.OK);
        }

        @Test
        @DisplayName("Verify it is possible to approve revoked consent")
        void possibleToApproveRevokedConsentTest() {
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
            new ResponseAssertion(approveResponse).statusCodeIsEqualTo(StatusCode.OK);

            final var expectedConsentRequestStatus = new ConsentRequestStatus().approved(1).pending(0).revoked(0);
            consentRequestController.withCMToken();
            var actualConsentRequest = consentRequestController.getStatusForConsentRequestById(crid);

            new ResponseAssertion(actualConsentRequest)
                    .statusCodeIsEqualTo(StatusCode.OK)
                    .responseIsEqualToObject(expectedConsentRequestStatus);
        }

    }

}