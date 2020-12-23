package com.here.platform.cm.consentStatus.accessToken;

import static org.assertj.core.api.Assertions.assertThat;

import com.here.platform.cm.consentStatus.BaseConsentStatusTests;
import com.here.platform.cm.controllers.AccessTokenController;
import com.here.platform.cm.controllers.ConsentStatusController.NewConsent;
import com.here.platform.cm.dataAdapters.ConsentContainerToNsContainer;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.Consents;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.AccessTokenResponse;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps2;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.GetAccessToken;
import com.here.platform.common.extensions.ConsentRequestRemoveExtension;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.dataProviders.reference.controllers.ReferenceTokenController;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.Steps;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


@GetAccessToken
@DisplayName("Get Access token")
class AccessTokenTests extends BaseConsentStatusTests {

    private final AccessTokenController accessTokenController = new AccessTokenController();

    @Test
    @DisplayName("Verify Getting Access Token For Revoked Consent")
    void getAccessTokenForRevokedConsentTest() {
        ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetApp.getProvider());

        DataSubjects dataSubject = DataSubjects.getNextVinLength(targetApp.getProvider().getVinLength());
        String testVin = dataSubject.getVin();

        ConsentInfo consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        var crid = new ConsentRequestSteps2(targetApp.getProvider().getName(), consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin)
                .getId();

        ConsentFlowSteps.revokeConsentForVIN(crid, testVin);

        final var actualAccessTokenResponse = accessTokenController
                .withConsumerToken()
                .getAccessToken(crid, testVin, mpConsumer.getRealm());

        String actualCause = new ResponseAssertion(actualAccessTokenResponse)
                .statusCodeIsEqualTo(StatusCode.UNAUTHORIZED)
                .expectedErrorResponse(CMErrorResponse.CONSENT_NOT_APPROVED)
                .getCause();
        assertThat(actualCause).isEqualTo("Consent not approved");
    }

    @Test
    @DisplayName("Verify Getting Access Token For Approved Consent")
    @Tag("cm_prod")
    void getAccessTokenForApprovedConsentTest() {
        ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetApp.getProvider());

        DataSubjects dataSubject = DataSubjects.getNextVinLength(targetApp.getProvider().getVinLength());
        String testVin = dataSubject.getVin();

        ConsentInfo consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        var crid = new ConsentRequestSteps2(targetApp.getProvider().getName(), consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin)
                .getId();

        ConsentFlowSteps.approveConsentForVIN(crid, targetContainer, testVin);

        fuSleep();
        final var actualResponse = accessTokenController
                .withConsumerToken()
                .getAccessToken(crid, testVin, mpConsumer.getRealm());
        var accessTokenResponse = new ResponseAssertion(actualResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(AccessTokenResponse.class);
        Assertions.assertThat(accessTokenResponse.getAccessToken()).isNotBlank();
        Assertions.assertThat(accessTokenResponse.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("Verify it is not possible to get Access Token with invalid ConsumerId")
    void isNotPossibleToGetAccessTokenWithInvalidConsumerIdTest() {
        ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetApp.getProvider());

        DataSubjects dataSubject = DataSubjects.getNextVinLength(targetApp.getProvider().getVinLength());
        String testVin = dataSubject.getVin();

        ConsentInfo consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        var crid = new ConsentRequestSteps2(targetApp.getProvider().getName(), consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin)
                .getId();

        ConsentFlowSteps.approveConsentForVIN(crid, targetContainer, testVin);

        final var actualResponse = accessTokenController
                .withConsumerToken()
                .getAccessToken(crid, testVin, mpConsumer.getRealm() + 1);

        String actualCause = new ResponseAssertion(actualResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.CONSENT_NOT_FOUND)
                .getCause();
        assertThat(actualCause).isEqualTo("Consent not found");
    }

    @Test
    @DisplayName("Verify it is possible to approve two consents for single VIN")
    void approveTwoConsentsForSingleVinTest() {
        ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer1 = ConsentRequestContainers.generateNew(targetApp.getProvider());
        ConsentRequestContainer targetContainer2 = ConsentRequestContainers.generateNew(targetApp.getProvider());

        DataSubjects dataSubject = DataSubjects.getNextVinLength(targetApp.getProvider().getVinLength());
        String testVin = dataSubject.getVin();

        ConsentInfo consentInfo1 = Consents.generateNewConsentInfo(mpConsumer, targetContainer1);
        ConsentInfo consentInfo2 = Consents.generateNewConsentInfo(mpConsumer, targetContainer2);
        var crid1 = new ConsentRequestSteps2(targetApp.getProvider().getName(), consentInfo1)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin)
                .getId();

        var crid2 = new ConsentRequestSteps2(targetApp.getProvider().getName(), consentInfo2)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin)
                .getId();

        ConsentFlowSteps.approveConsentForVIN(crid1, targetContainer1, testVin);

        var secondDaimlerToken = ReferenceTokenController
                .produceConsentAuthCode(testVin, targetContainer2.getId() + ":general");
        NewConsent secondConsumerConsent = NewConsent.builder()
                .consentRequestId(crid2)
                .vinHash(new VIN(testVin).hashed())
                .authorizationCode(secondDaimlerToken)
                .build();

        var secondApprovedConsentResponse = consentStatusController
                .approveConsent(secondConsumerConsent, dataSubject.getBearerToken());
        new ResponseAssertion(secondApprovedConsentResponse).statusCodeIsEqualTo(StatusCode.OK);

        consentRequestController.withConsumerToken();
        var secondConsentStatusResponse = consentRequestController
                .getStatusForConsentRequestById(crid2);

        new ResponseAssertion(secondConsentStatusResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(
                        new ConsentRequestStatus()
                                .approved(1)
                                .pending(0)
                                .revoked(0)
                                .expired(0)
                                .rejected(0)
                );
    }

}
