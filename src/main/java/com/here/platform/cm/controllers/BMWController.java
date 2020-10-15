package com.here.platform.cm.controllers;

import com.here.platform.common.config.Conf;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;


public class BMWController extends BaseConsentService<BMWController> {

    private final String bmwBasePath = StringUtils.EMPTY;
    private String bmwToken = Conf.cm().getBmwClearanceSecret();

    private void withBMWToken(String tokenValue) {
        bmwToken = tokenValue;
    }


    @Step("Ping consent service status by BMW")
    public Response pingConsentServiceStatusByBMW() {
        return consentServiceClient(bmwBasePath)
                .header("Authorization", bmwToken)
                .get("/status/ping");
    }

    @Step("Update clearance status with clearanceId: '{clearanceId}', with status: '{consentState}' by BMW")
    public Response setClearanceStatusByBMW(String clearanceId, String consentState) {
        return consentServiceClient(bmwBasePath)
                .header("Authorization", bmwToken)
                .put("clearances/{clearanceId}/status/{clearanceStatus}", clearanceId, consentState);
    }

}
