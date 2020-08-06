package com.here.platform.cm.bmw;

import com.here.platform.cm.consentStatus.BaseConsentStatusTests;
import com.here.platform.cm.controllers.BMWController;
import com.here.platform.cm.rest.model.Health;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Verify and Set Consent Clearance status by BMW Provider")
public class BmwRequestStatusTests extends BaseConsentStatusTests {

    private String crid;
    private final BMWController bmwController = new BMWController();

    @AfterEach
    void cleanUp() {
        RemoveEntitiesSteps.cascadeForceRemoveConsentRequest(crid, testFileWithVINs, testConsentRequestData);
    }

    @Test
    @Issue("NS-2427")
    @DisplayName("BMW read status of consent service acceptability")
    void pingConsentStatusByBMW() {
        var response = bmwController.pingConsentServiceStatusByBMW();

        new ResponseAssertion(response)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new Health().status("OK"));
    }

    @Test
    @Issue("NS-2427")
    @DisplayName("BMW set status of consent clearance")
    void setClearanceStatusByBMW() {
        crid = createValidConsentRequest();

        var privateBearer = dataSubject.getBearerToken();
        var responseBefore = consentStatusController
                .getConsentRequestInfoByVinAndCrid(testVin, crid, privateBearer);

        var response = bmwController.setClearanceStatusByBMW(crid, "APPROVED");

        new ResponseAssertion(response)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new Health().status("OK"));

        var responseAfter = consentStatusController
                .getConsentRequestInfoByVinAndCrid(testVin, crid, privateBearer);
    }

}
