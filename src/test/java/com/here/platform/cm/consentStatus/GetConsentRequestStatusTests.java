package com.here.platform.cm.consentStatus;


import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.CMFeatures.GetConsentRequestStatus;
import com.here.platform.common.annotations.Sentry;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Get consent request status")
@GetConsentRequestStatus
public class GetConsentRequestStatusTests extends BaseConsentStatusTests {

    private final User mpConsumer = Users.MP_CONSUMER.getUser();
    private String crid;

    @AfterEach
    void cleanUp() {
        if (Objects.nonNull(crid)) {
            RemoveEntitiesSteps.cascadeForceRemoveConsentRequest(crid, testFileWithVINs, testConsentRequestData);
        }
    }

    @Test
    @DisplayName("Verify Set Consent Status For Non Existent Consent Request Id")
    void getConsentStatusForNonExistentConsentRequestIdTest() {
        consentRequestController.withConsumerToken();
        var actualResponse = consentRequestController
                .getStatusForConsentRequestById(crypto.sha256());

        new ResponseAssertion(actualResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new ConsentRequestStatus()
                        .approved(0)
                        .pending(0)
                        .revoked(0)
                        .expired(0)
                        .rejected(0)
                );
    }

    @Test
    @Sentry
    @DisplayName("Verify Sentry Block Getting ConsentRequest Statuses")
    void sentryBlockGettingConsentRequestStatusesTest() {
        consentRequestController.clearBearerToken();
        var actualResponse = consentRequestController
                .getStatusForConsentRequestById("not_found" + crypto.sha512());

        new ResponseAssertion(actualResponse).statusCodeIsEqualTo(StatusCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Verify Get ConsentRequest In All Statuses")
    void getConsentRequestInAllStatusesTest() {
        String vinToApprove = testVin,
                vinToRevoke = DataSubjects.getNextVinLength(targetApp.provider.vinLength).getVin(),
                vinToPending = DataSubjects.getNextVinLength(targetApp.provider.vinLength).getVin();

        crid = createValidConsentRequest();
        consentRequestController.withAuthorizationValue(mpConsumer.getToken());
        testFileWithVINs = new VinsToFile(testVin, vinToRevoke, vinToPending).csv();
        consentRequestController
                .addVinsToConsentRequest(crid, new VinsToFile(testVin, vinToRevoke, vinToPending).csv());

        fuSleep();
        ConsentFlowSteps.approveConsentForVIN(crid, testContainer, vinToApprove);

        fuSleep();
        ConsentFlowSteps.revokeConsentForVIN(crid, vinToRevoke);

        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(1)
                .pending(1)
                .revoked(1)
                .expired(0)
                .rejected(0);

        consentRequestController.withConsumerToken();
        var statusForConsentRequestByIdResponse = consentRequestController
                .getStatusForConsentRequestById(crid);
        new ResponseAssertion(statusForConsentRequestByIdResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedConsentRequestStatuses);
    }

}
