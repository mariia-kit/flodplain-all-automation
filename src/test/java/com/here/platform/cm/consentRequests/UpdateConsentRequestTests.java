package com.here.platform.cm.consentRequests;

import static com.here.platform.common.strings.SBB.sbb;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.Consents;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps2;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.AAA;
import com.here.platform.common.annotations.CMFeatures.ApproveConsent;
import com.here.platform.common.annotations.CMFeatures.RevokeConsent;
import com.here.platform.common.annotations.CMFeatures.UpdateConsentRequest;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@UpdateConsentRequest
@DisplayName("Update consent request")
public class UpdateConsentRequestTests extends BaseCMTest {

    private final ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;

    private final String messageForbiddenToRemoveApproved =
            sbb("All non-approved VINs have been deleted.").w()
                    .append("Please use RemoveAllDataSubjects endpoint if you wish to delete all approved VINs")
                    .bld();

    @Test
    @AAA
    @DisplayName("Verify Adding Vins To ConsentRequest")
    void addVinsToConsentRequestTest() {
        User mpConsumer = Users.MP_CONSUMER.getUser();
        MPProviders targetProvider = MPProviders.DAIMLER_REFERENCE;
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetProvider);
        ConsentInfo consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        String vin1 = VIN.generate(targetProvider.getVinLength());

        new ConsentRequestSteps2(targetContainer.getProvider().getName(), consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vin1)
                .verifyConsentStatus(
                        new ConsentRequestStatus()
                        .approved(0)
                        .pending(1)
                        .revoked(0)
                        .expired(0)
                        .rejected(0))
                .getId();
    }

    @Test
    @AAA
    @DisplayName("Verify Remove Vins From ConsentRequest")
    void removeVinsFromConsentRequestTest() {
        User mpConsumer = Users.MP_CONSUMER.getUser();
        MPProviders targetProvider = MPProviders.DAIMLER_REFERENCE;
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetProvider);
        ConsentInfo consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        String vin1 = VIN.generate(targetProvider.getVinLength());
        String vin2 = VIN.generate(targetProvider.getVinLength());
        String vin3 = VIN.generate(targetProvider.getVinLength());

        new ConsentRequestSteps2(targetContainer.getProvider().getName(), consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vin1, vin2, vin3)
                .verifyConsentStatus(
                        new ConsentRequestStatus()
                        .approved(0)
                        .pending(3)
                        .revoked(0)
                        .expired(0)
                        .rejected(0))
                .removeVINsFromConsentRequest(vin1, vin2)
                .verifyConsentStatusByVin(vin3, "PENDING");
//        consentRequestController.withConsumerToken(mpConsumer);
//        consentRequestController.addVinsToConsentRequest(crid, new VinsToFile(vin1, vin2, vin3).csv());
//        fuSleep();
//        var expectedConsentRequestStatuses = new ConsentRequestStatus()
//                .approved(0)
//                .pending(3)
//                .revoked(0)
//                .expired(0)
//                .rejected(0);
//
//        consentRequestController.withConsumerToken();
//        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
//                .responseIsEqualToObject(expectedConsentRequestStatuses);
//
//        fuSleep();
//        consentRequestController.removeVinsFromConsentRequest(crid, new VinsToFile(vin1, vin2).json());
//
//        expectedConsentRequestStatuses.pending(1);
//        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
//                .responseIsEqualToObject(expectedConsentRequestStatuses);
//
//        var consentStatusByIdAndVinResponse = new ConsentStatusController()
//                .withConsumerToken(mpConsumer)
//                .getConsentStatusByIdAndVin(crid, vin3);
//        new ResponseAssertion(consentStatusByIdAndVinResponse).statusCodeIsEqualTo(StatusCode.OK)
//                .responseIsEqualToObject(
//                );
    }

    @Test
    @ApproveConsent
    @DisplayName("Force remove approved consents from consent request")
    void forceRemoveApprovedDataSubjectsTest() {
        User mpConsumer = Users.MP_CONSUMER.getUser();
        MPProviders targetProvider = MPProviders.DAIMLER_REFERENCE;
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetProvider);
        ConsentInfo consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        var vinToApprove = DataSubjects.getNextVinLength(targetProvider.getVinLength()).getVin();
        String vin2 = VIN.generate(targetProvider.getVinLength());
        String vin3 = VIN.generate(targetProvider.getVinLength());

        ConsentRequestSteps2 steps = new ConsentRequestSteps2(targetContainer.getProvider().getName(), consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vinToApprove, vin2, vin3);

        ConsentFlowSteps.approveConsentForVIN(steps.getId(), targetContainer, vinToApprove);

        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(1)
                .pending(2)
                .revoked(0)
                .expired(0)
                .rejected(0);

        steps.verifyConsentStatus(expectedConsentRequestStatuses);
        consentRequestController
                .withAuthorizationValue(Users.MP_CONSUMER.getToken())
                .forceRemoveVinsFromConsentRequest(steps.getId(), new VinsToFile(vinToApprove, vin2).csv());

        steps.verifyConsentStatus(expectedConsentRequestStatuses.approved(0).pending(1));
    }

    @Test
    @DisplayName("Force remove revoked consent from consent request")
    @RevokeConsent
    void forceRemoveRevokedConsentsTest() {
        User mpConsumer = Users.MP_CONSUMER.getUser();
        MPProviders targetProvider = MPProviders.DAIMLER_REFERENCE;
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetProvider);
        ConsentInfo consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        var vinToRevoke  = DataSubjects.getNextVinLength(targetProvider.getVinLength()).getVin();
        String vin2 = VIN.generate(targetProvider.getVinLength());
        String vin3 = VIN.generate(targetProvider.getVinLength());

        ConsentRequestSteps2 steps = new ConsentRequestSteps2(targetContainer.getProvider().getName(), consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vinToRevoke, vin2, vin3);

        ConsentFlowSteps.revokeConsentForVIN(steps.getId(), vinToRevoke);

        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(0)
                .pending(2)
                .revoked(1)
                .expired(0)
                .rejected(0);

        steps.verifyConsentStatus(expectedConsentRequestStatuses);

        var removeResp = consentRequestController
                .withAuthorizationValue(Users.MP_CONSUMER.getToken())
                .forceRemoveVinsFromConsentRequest(steps.getId(), new VinsToFile(vinToRevoke, vin2).csv());
        new ResponseAssertion(removeResp).statusCodeIsEqualTo(StatusCode.OK);

        steps.verifyConsentStatus(expectedConsentRequestStatuses.pending(1).revoked(0));
    }

    @Test
    @DisplayName("Remove revoked consent from consent request")
    @RevokeConsent
    void removeRevokedConsentsTest() {
        User mpConsumer = Users.MP_CONSUMER.getUser();
        MPProviders targetProvider = MPProviders.DAIMLER_REFERENCE;
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetProvider);
        ConsentInfo consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        var vinToRevoke  = DataSubjects.getNextVinLength(targetProvider.getVinLength()).getVin();
        String vin2 = VIN.generate(targetProvider.getVinLength());
        String vin3 = VIN.generate(targetProvider.getVinLength());

        ConsentRequestSteps2 steps = new ConsentRequestSteps2(targetContainer.getProvider().getName(), consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vinToRevoke, vin2, vin3);

        ConsentFlowSteps.revokeConsentForVIN(steps.getId(), vinToRevoke);

        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(0)
                .pending(2)
                .revoked(1)
                .rejected(0)
                .expired(0);

        steps.verifyConsentStatus(expectedConsentRequestStatuses)
                .removeVINsFromConsentRequest(vinToRevoke, vin2)
                .verifyConsentStatus(expectedConsentRequestStatuses.pending(1).revoked(0));
    }

    @Test
    @ApproveConsent
    @DisplayName("Forbidden to remove approved consent")
    void forbiddenToRemoveApprovedDataSubjects() {
        User mpConsumer = Users.MP_CONSUMER.getUser();
        MPProviders targetProvider = MPProviders.DAIMLER_REFERENCE;
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetProvider);
        ConsentInfo consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        var vinToApprove = DataSubjects.getNextVinLength(targetProvider.getVinLength()).getVin();
        String vin2 = VIN.generate(targetProvider.getVinLength());
        String vin3 = VIN.generate(targetProvider.getVinLength());

        ConsentRequestSteps2 steps = new ConsentRequestSteps2(targetContainer.getProvider().getName(), consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vinToApprove, vin2, vin3);
//        var vinToApprove = DataSubjects.getNextVinLength(vinLength).getVin();
//        consentRequestController.withConsumerToken(mpConsumer);
//        testFileWithVINs = new VinsToFile(vinToApprove, vin2, vin3).csv();
//        consentRequestController.addVinsToConsentRequest(crid, testFileWithVINs);

        ConsentFlowSteps.approveConsentForVIN(steps.getId(), targetContainer, vinToApprove);

        //remove consents in approved and pending state

        var removeVinsFromConsentRequest = consentRequestController
                .withConsumerToken()
                .removeVinsFromConsentRequest(steps.getId(), new VinsToFile(vinToApprove, vin2).json());
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

        steps.verifyConsentStatus(expectedConsentRequestStatuses);
    }

}
