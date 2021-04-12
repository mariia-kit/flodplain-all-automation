package com.here.platform.cm.consentStatus;


import static com.here.platform.common.strings.SBB.sbb;

import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.GetConsentRequestStatus;
import com.here.platform.common.annotations.Sentry;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@GetConsentRequestStatus
@Disabled
@DisplayName("Get Consent status")
public class GetConsentRequestStatusTests extends BaseConsentStatusTests {

    @Test
    @DisplayName("Verify get Consent request status for absent Consent request")
    void getConsentStatusForNonExistentConsentRequestIdTest() {
        consentRequestController.withConsumerToken();
        var actualResponse = consentRequestController
                .getStatusForConsentRequestById(crypto.sha256());

        new ResponseAssertion(actualResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new ConsentRequestStatus()
                        .approved(0).pending(0).revoked(0).expired(0).rejected(0)
                );
    }

    @Test
    @Sentry
    @DisplayName("Verify sentry block fetting Consent request status with empty Authorization token")
    void sentryBlockGettingConsentRequestStatusesTest() {
        consentRequestController.clearBearerToken();
        var actualResponse = consentRequestController
                .getStatusForConsentRequestById(sbb("not_found").append(crypto.sha512()).bld());

        new ResponseAssertion(actualResponse).statusCodeIsEqualTo(StatusCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Verify get Consent request in all statuses")
    void getConsentRequestInAllStatusesTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);
        String vinToApprove = DataSubjects.getNextVinLength(provider.getVinLength()).getVin(),
                vinToRevoke = DataSubjects.getNextVinLength(provider.getVinLength()).getVin(),
                vinToPending = DataSubjects.getNextVinLength(provider.getVinLength()).getVin();

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        ConsentRequestSteps step = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vinToApprove, vinToRevoke, vinToPending);

        ConsentFlowSteps.approveConsentForVIN(step.getId(), targetContainer, vinToApprove);

        ConsentFlowSteps.revokeConsentForVIN(step.getId(), vinToRevoke);

        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(1)
                .pending(1)
                .revoked(1)
                .expired(0)
                .rejected(0);

        step.verifyConsentStatus(expectedConsentRequestStatuses);
    }

}
