package com.here.platform.cm.steps;


import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.common.VIN;
import com.here.platform.dataProviders.DaimlerTokenController;
import com.here.platform.dataProviders.DataSubjects;
import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;


@UtilityClass
public class ConsentFlowSteps {

    @Step
    public void approveConsentForVIN(String crid, ConsentRequestContainers container, String targetVIN) {
        var validDaimlerToken = new DaimlerTokenController(targetVIN, container).generateAuthorizationCode();
        final var consentToApprove = ConsentStatusController.NewConsent.builder()
                .vinHash(new VIN(targetVIN).hashed())
                .consentRequestId(crid)
                .authorizationCode(validDaimlerToken)
                .build();

        var privateBearer = DataSubjects.getByVin(targetVIN).getBearerToken();
        var approveResponse = new ConsentStatusController().approveConsent(consentToApprove, privateBearer);
        StepExpects.expectOKStatusCode(approveResponse);
    }

    @Step
    public void revokeConsentForVIN(String crid, String targetVIN) {
        var consentToRevoke = ConsentStatusController.NewConsent.builder()
                .consentRequestId(crid)
                .vinHash(new VIN(targetVIN).hashed())
                .build();

        var privateBearer = DataSubjects.getByVin(targetVIN).getBearerToken();
        var revokeResponse = new ConsentStatusController().revokeConsent(consentToRevoke, privateBearer);
        StepExpects.expectOKStatusCode(revokeResponse);
    }

}
