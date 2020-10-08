package com.here.platform.cm.steps.api;


import com.here.platform.aaa.ReferenceTokenController;
import com.here.platform.cm.controllers.BMWController;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.controllers.ConsentStatusController.NewConsent;
import com.here.platform.cm.enums.BMWStatus;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.common.VIN;
import com.here.platform.common.controller.ReferenceProviderController;
import com.here.platform.dataProviders.daimler.DaimlerTokenController;
import com.here.platform.dataProviders.daimler.DataSubjects;
import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;
import org.apache.http.HttpStatus;


@UtilityClass
public class ConsentFlowSteps {

    @Step
    public void approveConsentForVIN(String crid, ConsentRequestContainers container, String targetVIN) {
        String validCode;
        String token = DataSubjects.getByVin(targetVIN).getBearerToken();

        if (container.getProvider().equals(MPProviders.DAIMLER_EXPERIMENTAL_REFERENCE)) {
            validCode = ReferenceTokenController
                    .produceConsentAuthCode(targetVIN, container.getId() + ":general");
        } else {
            validCode = new DaimlerTokenController(targetVIN, container).generateAuthorizationCode();
        }
        var consentToApprove = ConsentStatusController.NewConsent.builder()
                .vinHash(new VIN(targetVIN).hashed())
                .consentRequestId(crid)
                .authorizationCode(validCode)
                .build();
        var approveResponse = new ConsentStatusController().approveConsent(consentToApprove, token);

        StepExpects.expectOKStatusCode(approveResponse);
    }

    @Step
    public void approveConsentForVinBMW(String bmwContainer, String targetVIN) {
        var clearanceId = new ReferenceProviderController().getClearanceByVin(targetVIN, bmwContainer).jsonPath()
                .get("clearanceId").toString();
        var response = new BMWController().setClearanceStatusByBMW(clearanceId, BMWStatus.APPROVED.name());
        StepExpects.expectOKStatusCode(response);
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
