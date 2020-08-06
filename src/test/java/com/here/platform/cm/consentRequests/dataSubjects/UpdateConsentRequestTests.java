package com.here.platform.cm.consentRequests.dataSubjects;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.enums.DaimlerContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.rest.model.ConsentStatus;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.AAA;
import com.here.platform.common.annotations.CMFeatures.ApproveConsent;
import com.here.platform.common.annotations.CMFeatures.RevokeConsent;
import com.here.platform.common.annotations.CMFeatures.UpdateConsentRequest;
import com.here.platform.dataProviders.daimler.DataSubjects;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Update consent request")
@UpdateConsentRequest
public class UpdateConsentRequestTests extends BaseCMTest {

    private final ProviderApplications targetApp = ProviderApplications.DAIMLER_CONS_1;
    private final int vinLength = targetApp.provider.vinLength;

    private final String
            vin1 = VIN.generate(vinLength),
            vin2 = VIN.generate(vinLength),
            vin3 = VIN.generate(vinLength);
    private final DaimlerContainers testContainer = targetApp.container;
    private final MPConsumers mpConsumer = targetApp.consumer;
    private final ConsentRequestData testConsentRequest = new ConsentRequestData()
            .consumerId(mpConsumer.getRealm())
            .providerId(targetApp.provider.getName())
            .title(faker.gameOfThrones().quote())
            .purpose(faker.commerce().productName())
            .privacyPolicy(faker.internet().url())
            .containerId(testContainer.id);

    private final String messageForbiddenToRemoveApproved =
            "All non-approved VINs have been deleted. "
                    + "Please use RemoveAllDataSubjects endpoint if you wish to delete all approved VINs";
    private String crid;

    @BeforeEach
    void onboardApplicationForProviderAndConsumer() {
        OnboardingSteps.onboardApplicationProviderAndConsumer(
                testConsentRequest.getProviderId(),
                testConsentRequest.getConsumerId(),
                testContainer
        );
        consentRequestController.withCMToken();
        crid = new ResponseAssertion(consentRequestController.createConsentRequest(testConsentRequest))
                .statusCodeIsEqualTo(StatusCode.CREATED)
                .bindAs(ConsentRequestIdResponse.class)
                .getConsentRequestId();
    }

    @Test
    @AAA
    @DisplayName("Verify Adding Vins To ConsentRequest")
    void addVinsToConsentRequestTest() {
        var addVinsToConsentRequest = consentRequestController
                .withConsumerToken(mpConsumer)
                .addVinsToConsentRequest(crid, new VinsToFile(vin1).json());
        new ResponseAssertion(addVinsToConsentRequest).statusCodeIsEqualTo(StatusCode.OK);

        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(new ConsentRequestStatus()
                        .approved(0)
                        .pending(1)
                        .revoked(0)
                );
    }

    @Test
    @AAA
    @DisplayName("Verify Remove Vins From ConsentRequest")
    void removeVinsFromConsentRequestTest() {
        consentRequestController.withConsumerToken(mpConsumer);
        consentRequestController.addVinsToConsentRequest(crid, new VinsToFile(vin1, vin2, vin3).csv());
        fuSleep();
        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(0)
                .pending(3)
                .revoked(0);

        consentRequestController.withCMToken();
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);

        fuSleep();
        consentRequestController.removeVinsFromConsentRequest(crid, new VinsToFile(vin1, vin2).json());

        expectedConsentRequestStatuses.pending(1);
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);

        var consentStatusByIdAndVinResponse = new ConsentStatusController()
                .withConsumerToken(mpConsumer)
                .getConsentStatusByIdAndVin(crid, vin3);
        new ResponseAssertion(consentStatusByIdAndVinResponse).statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new ConsentStatus()
                        .consentRequestId(crid)
                        .vin(vin3)
                        .state("PENDING")
                );
    }

    @Test
    @ApproveConsent
    @DisplayName("Force remove approved consents from consent request")
    void forceRemoveApprovedDataSubjectsTest() {
        var vinToApprove = DataSubjects.getNext().vin;
        consentRequestController
                .withConsumerToken(mpConsumer)
                .addVinsToConsentRequest(crid, new VinsToFile(vinToApprove, vin2, vin3).json());

        ConsentFlowSteps.approveConsentForVIN(crid, testContainer, vinToApprove);
        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(1)
                .pending(2)
                .revoked(0);
        consentRequestController.withCMToken();
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);

        fuSleep();
        consentRequestController.forceRemoveVinsFromConsentRequest(crid, new VinsToFile(vinToApprove, vin2).csv());
        fuSleep();
        expectedConsentRequestStatuses.approved(0).pending(1);
        consentRequestController.withCMToken();
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);
    }

    @Test
    @DisplayName("Force remove revoked consent from consent request")
    @RevokeConsent
    void forceRemoveRevokedConsentsTest() {
        var vehicle = DataSubjects.getNext();
        var vinToRevoke = vehicle.vin;
        consentRequestController.withConsumerToken(mpConsumer);
        consentRequestController.addVinsToConsentRequest(crid, new VinsToFile(vinToRevoke, vin2, vin3).json());

        fuSleep();
        ConsentFlowSteps.revokeConsentForVIN(crid, vinToRevoke);

        fuSleep();
        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(0)
                .pending(2)
                .revoked(1);
        consentRequestController.withCMToken();
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);

        fuSleep();
        var removeResp = consentRequestController
                .forceRemoveVinsFromConsentRequest(crid, new VinsToFile(vinToRevoke, vin2).csv());
        new ResponseAssertion(removeResp).statusCodeIsEqualTo(StatusCode.OK);

        fuSleep();
        expectedConsentRequestStatuses.pending(1).revoked(0);
        consentRequestController.withCMToken();
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);
    }

    @Test
    @DisplayName("Remove revoked consent from consent request")
    @RevokeConsent
    void removeRevokedConsentsTest() {
        var targetDataSubject = DataSubjects.getNext();
        var vinToRevoke = targetDataSubject.vin;
        consentRequestController
                .withConsumerToken(mpConsumer)
                .addVinsToConsentRequest(crid, new VinsToFile(vinToRevoke, vin2, vin3).csv());

        fuSleep();
        ConsentFlowSteps.revokeConsentForVIN(crid, vinToRevoke);

        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(0)
                .pending(2)
                .revoked(1);
        consentRequestController.withCMToken();
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);

        fuSleep();
        consentRequestController
                .removeVinsFromConsentRequest(crid, new VinsToFile(vinToRevoke, vin2).json());

        expectedConsentRequestStatuses.pending(1).revoked(0);
        consentRequestController.withCMToken();
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);
    }

    @Test
    @ApproveConsent
    @DisplayName("Forbidden to remove approved consent")
    void forbiddenToRemoveApprovedDataSubjects() {
        var vinToApprove = DataSubjects.getNext().vin;
        consentRequestController.withConsumerToken(mpConsumer);
        consentRequestController.addVinsToConsentRequest(crid, new VinsToFile(vinToApprove, vin2, vin3).csv());

        ConsentFlowSteps.approveConsentForVIN(crid, testContainer, vinToApprove);

        fuSleep();
        //remove consents in approved and pending state
        var removeVinsFromConsentRequest = consentRequestController
                .removeVinsFromConsentRequest(crid, new VinsToFile(vinToApprove, vin2).json());
        //check response have forbidden VIN
        Assertions.assertThat(removeVinsFromConsentRequest.body().asString())
                .isEqualTo(String.format(
                        "{\"approvedVINs\":[\"%s\"],"
                                + "\"message\":\"%s\"}",
                        vinToApprove, messageForbiddenToRemoveApproved
                ));

        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(1)
                .pending(1)
                .revoked(0);
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);
    }

}
