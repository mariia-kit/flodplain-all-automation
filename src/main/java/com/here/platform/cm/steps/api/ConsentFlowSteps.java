package com.here.platform.cm.steps.api;


import com.here.platform.cm.controllers.BMWController;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.enums.BMWStatus;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DaimlerTokenController;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.dataProviders.reference.controllers.ReferenceProviderController;
import com.here.platform.dataProviders.reference.controllers.ReferenceTokenController;
import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;


@UtilityClass
public class ConsentFlowSteps {

    @Step
    public void approveConsentForVIN(String crid, ConsentRequestContainer container, String targetVIN) {
        String token = DataSubjects.getByVin(targetVIN).getBearerToken();
        approveConsentForVIN(crid, container, targetVIN, token);
    }

    @Step
    public void approveConsentForVIN(String crid, ConsentRequestContainer container, String targetVIN, String token) {
        String validCode;

        if (container.getProvider().equals(MPProviders.DAIMLER_REFERENCE)) {
            validCode = ReferenceTokenController.produceConsentAuthCode(targetVIN, container.getId() + ":general");
        } else {
            validCode = new DaimlerTokenController(targetVIN, container).generateAuthorizationCode();
        }
        var consentToApprove = ConsentStatusController.NewConsent.builder()
                .vinHash(new VIN(targetVIN).hashed())
                .consentRequestId(crid)
                .authorizationCode(validCode)
                .build();
        var approveResponse = new ConsentStatusController().approveConsent(consentToApprove, token);

        StatusCodeExpects.expectOKStatusCode(approveResponse);
    }

    @Step
    public void approveConsentForVinBMW(String bmwContainer, String targetVIN) {
        var clearanceId = new ReferenceProviderController().getClearanceByVinAndContainerId(targetVIN, bmwContainer).jsonPath()
                .get("clearanceId").toString();
        var response = new BMWController().setClearanceStatusByBMW(clearanceId, BMWStatus.APPROVED.name());
        StatusCodeExpects.expectOKStatusCode(response);
    }

    @Step
    public void revokeConsentForVIN(String crid, String targetVIN) {
        var privateBearer = DataSubjects.getByVin(targetVIN).getBearerToken();
        revokeConsentForVIN(crid, targetVIN, privateBearer);
    }

    @Step
    public void revokeConsentForVIN(String crid, String targetVIN, String token) {
        var consentToRevoke = ConsentStatusController.NewConsent.builder()
                .consentRequestId(crid)
                .vinHash(new VIN(targetVIN).hashed())
                .build();
        var revokeResponse = new ConsentStatusController().revokeConsent(consentToRevoke, token);
        StatusCodeExpects.expectOKStatusCode(revokeResponse);
    }

}
