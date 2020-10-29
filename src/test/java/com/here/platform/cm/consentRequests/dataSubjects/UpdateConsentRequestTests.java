package com.here.platform.cm.consentRequests.dataSubjects;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.rest.model.ConsentStatus;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.AAA;
import com.here.platform.common.annotations.CMFeatures.ApproveConsent;
import com.here.platform.common.annotations.CMFeatures.RevokeConsent;
import com.here.platform.common.annotations.CMFeatures.UpdateConsentRequest;
import com.here.platform.common.config.Conf;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.helpers.Steps;
import java.io.File;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Update consent request")
@UpdateConsentRequest
public class UpdateConsentRequestTests extends BaseCMTest {

    private final ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;
    private final int vinLength = targetApp.provider.vinLength;

    private final String
            vin1 = VIN.generate(vinLength),
            vin2 = VIN.generate(vinLength),
            vin3 = VIN.generate(vinLength);

    private File testFileWithVINs = new VinsToFile(vin1, vin2, vin3).csv();

    protected final MPConsumers mpConsumer = targetApp.consumer;
    protected DataSubjects dataSubject = DataSubjects._2AD190A6AD057824E;
    protected ConsentRequestContainer testContainer = ConsentRequestContainers.generateNew(targetApp.provider.getName());
    
    private final ConsentRequestData testConsentRequest = new ConsentRequestData()
            .consumerId(mpConsumer.getRealm())
            .providerId(targetApp.provider.getName())
            .title(Conf.cm().getQaTestDataMarker() + faker.gameOfThrones().quote())
            .purpose(faker.commerce().productName())
            .privacyPolicy(faker.internet().url())
            .containerId(testContainer.getId());

    private final String messageForbiddenToRemoveApproved =
            "All non-approved VINs have been deleted. "
                    + "Please use RemoveAllDataSubjects endpoint if you wish to delete all approved VINs";
    private String crid;

    @BeforeEach
    void onboardApplicationForProviderAndConsumer() {
        Steps.createRegularContainer(testContainer);
        OnboardingSteps onboard = new OnboardingSteps(targetApp.provider, targetApp.consumer.getRealm());
        onboard.onboardTestProviderApplication(
                testContainer.getName(),
                targetApp.container.clientId,
                targetApp.container.clientSecret);
        consentRequestController.withConsumerToken();
        crid = new ResponseAssertion(consentRequestController.createConsentRequest(testConsentRequest))
                .statusCodeIsEqualTo(StatusCode.CREATED)
                .bindAs(ConsentRequestIdResponse.class)
                .getConsentRequestId();
    }

    @AfterEach
    void cleanUp() {
        RemoveEntitiesSteps.cascadeForceRemoveConsentRequest(crid, testFileWithVINs, testConsentRequest);
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
                        .expired(0)
                        .rejected(0)
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
                .revoked(0)
                .expired(0)
                .rejected(0);

        consentRequestController.withConsumerToken();
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
        var vinToApprove = DataSubjects.getNextVinLength(targetApp.provider.vinLength).getVin();
        testFileWithVINs = new VinsToFile(vinToApprove, vin2, vin3).json();
        consentRequestController
                .withConsumerToken(mpConsumer)
                .addVinsToConsentRequest(crid, testFileWithVINs);

        ConsentFlowSteps.approveConsentForVIN(crid, testContainer, vinToApprove);
        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(1)
                .pending(2)
                .revoked(0)
                .expired(0)
                .rejected(0);
        consentRequestController.withConsumerToken();
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);

        fuSleep();
        consentRequestController.forceRemoveVinsFromConsentRequest(crid, new VinsToFile(vinToApprove, vin2).csv());
        fuSleep();
        expectedConsentRequestStatuses.approved(0).pending(1);
        consentRequestController.withConsumerToken();
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);
    }

    @Test
    @DisplayName("Force remove revoked consent from consent request")
    @RevokeConsent
    void forceRemoveRevokedConsentsTest() {
        var vehicle = DataSubjects.getNextVinLength(targetApp.provider.vinLength);
        var vinToRevoke = vehicle.getVin();
        consentRequestController.withConsumerToken(mpConsumer);
        testFileWithVINs = new VinsToFile(vinToRevoke, vin2, vin3).json();
        consentRequestController.addVinsToConsentRequest(crid, testFileWithVINs);

        fuSleep();
        ConsentFlowSteps.revokeConsentForVIN(crid, vinToRevoke);

        fuSleep();
        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(0)
                .pending(2)
                .revoked(1)
                .expired(0)
                .rejected(0);
        consentRequestController.withConsumerToken();
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);

        fuSleep();
        var removeResp = consentRequestController
                .forceRemoveVinsFromConsentRequest(crid, new VinsToFile(vinToRevoke, vin2).csv());
        new ResponseAssertion(removeResp).statusCodeIsEqualTo(StatusCode.OK);

        fuSleep();
        expectedConsentRequestStatuses.pending(1).revoked(0);
        consentRequestController.withConsumerToken();
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);
    }

    @Test
    @DisplayName("Remove revoked consent from consent request")
    @RevokeConsent
    void removeRevokedConsentsTest() {
        var targetDataSubject = DataSubjects.getNextVinLength(targetApp.provider.vinLength);
        var vinToRevoke = targetDataSubject.getVin();
        testFileWithVINs = new VinsToFile(vinToRevoke, vin2, vin3).csv();
        consentRequestController
                .withConsumerToken(mpConsumer)
                .addVinsToConsentRequest(crid, testFileWithVINs);

        fuSleep();
        ConsentFlowSteps.revokeConsentForVIN(crid, vinToRevoke);

        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(0)
                .pending(2)
                .revoked(1)
                .rejected(0)
                .expired(0);
        consentRequestController.withConsumerToken();
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);

        fuSleep();
        consentRequestController
                .removeVinsFromConsentRequest(crid, new VinsToFile(vinToRevoke, vin2).json());

        expectedConsentRequestStatuses.pending(1).revoked(0);
        consentRequestController.withConsumerToken();
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);
    }

    @Test
    @ApproveConsent
    @DisplayName("Forbidden to remove approved consent")
    void forbiddenToRemoveApprovedDataSubjects() {
        var vinToApprove = DataSubjects.getNextVinLength(targetApp.provider.vinLength).getVin();
        consentRequestController.withConsumerToken(mpConsumer);
        testFileWithVINs = new VinsToFile(vinToApprove, vin2, vin3).csv();
        consentRequestController.addVinsToConsentRequest(crid, testFileWithVINs);

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
                .revoked(0)
                .expired(0)
                .rejected(0);
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(expectedConsentRequestStatuses);
    }

}
