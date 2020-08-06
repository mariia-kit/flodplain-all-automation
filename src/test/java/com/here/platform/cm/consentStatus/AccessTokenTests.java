package com.here.platform.cm.consentStatus;

import static org.assertj.core.api.Assertions.assertThat;

import com.here.platform.cm.controllers.AccessTokenController;
import com.here.platform.cm.controllers.ConsentStatusController.NewConsent;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.rest.model.AccessTokenResponse;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.annotations.CMFeatures.GetAccessToken;
import com.here.platform.dataProviders.daimler.DaimlerTokenController;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


@DisplayName("Getting of access tokens for consents")
@GetAccessToken
class AccessTokenTests extends BaseConsentStatusTests {

    private final AccessTokenController accessTokenController = new AccessTokenController();
    private String crid;

    @BeforeEach
    void createConsentRequest() {
        this.crid = createValidConsentRequest();
    }

    @AfterEach
    void cleanUp() {
        RemoveEntitiesSteps.cascadeForceRemoveConsentRequest(crid, testFileWithVINs, testConsentRequestData);
    }

    @Test
    @DisplayName("Verify Getting Access Token For Revoked Consent")
    void getAccessTokenForRevokedConsentTest() {
        ConsentFlowSteps.revokeConsentForVIN(crid, testVin);

        accessTokenController.withCMToken();
        final var actualAccessTokenResponse = accessTokenController
                .getAccessToken(this.crid, testVin, testConsumerId);

        String actualCause = new ResponseAssertion(actualAccessTokenResponse)
                .statusCodeIsEqualTo(StatusCode.UNAUTHORIZED)
                .expectedErrorResponse(CMErrorResponse.CONSENT_NOT_APPROVED)
                .getCause();
        assertThat(actualCause).isEqualTo("Consent not approved");
    }

    @Nested
    @GetAccessToken
    @DisplayName("Getting of access tokens for consents")
    public class ApprovedAccessTokens {

        @Test
        @DisplayName("Verify Getting Access Token For Approved Consent")
        void getAccessTokenForApprovedConsentTest() {
            ConsentFlowSteps.approveConsentForVIN(crid, testContainer, testVin);

            fuSleep();
            accessTokenController.withCMToken();
            final var actualResponse = accessTokenController
                    .getAccessToken(crid, testVin, testConsumerId);
            var accessTokenResponse = new ResponseAssertion(actualResponse)
                    .statusCodeIsEqualTo(StatusCode.OK)
                    .bindAs(AccessTokenResponse.class);
            Assertions.assertThat(accessTokenResponse.getAccessToken()).isNotBlank();
            Assertions.assertThat(accessTokenResponse.getTokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("Verify It Is Not Possible To Get Access Token With Invalid ConsumerId")
        void isNotPossibleToGetAccessTokenWithInvalidConsumerIdTest() {
            ConsentFlowSteps.approveConsentForVIN(crid, testContainer, testVin);

            accessTokenController.withCMToken();
            final var actualResponse = accessTokenController
                    .getAccessToken(crid, testVin, testConsumerId + 1);

            String actualCause = new ResponseAssertion(actualResponse)
                    .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                    .expectedErrorResponse(CMErrorResponse.CONSENT_NOT_FOUND)
                    .getCause();
            assertThat(actualCause).isEqualTo("Consent not found");
        }

        @Test
        @DisplayName("Verify It Is Possible To Approve Two Consents For Single Vin")
        void approveTwoConsentsForSingleVinTest() {
            ConsentFlowSteps.approveConsentForVIN(crid, testContainer, testVin);

            fuSleep();
            var secondConsentRequestId = createValidConsentRequest(); //second time with the same VIN

            var secondDaimlerToken = new DaimlerTokenController(testVin, testContainer).generateAuthorizationCode();
            NewConsent secondConsumerConsent = NewConsent.builder()
                    .consentRequestId(secondConsentRequestId)
                    .vinHash(new VIN(testVin).hashed())
                    .authorizationCode(secondDaimlerToken)
                    .build();

            var privateBearer = dataSubject.getBearerToken();

            var secondApprovedConsentResponse = consentStatusController
                    .approveConsent(secondConsumerConsent, privateBearer);
            new ResponseAssertion(secondApprovedConsentResponse).statusCodeIsEqualTo(StatusCode.OK);

            consentRequestController.withCMToken();
            var secondConsentStatusResponse = consentRequestController
                    .getStatusForConsentRequestById(secondConsentRequestId);

            new ResponseAssertion(secondConsentStatusResponse)
                    .statusCodeIsEqualTo(StatusCode.OK)
                    .responseIsEqualToObject(
                            new ConsentRequestStatus().approved(1).pending(0).revoked(0)
                    );
        }

    }

}
