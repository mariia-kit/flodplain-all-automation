package com.here.platform.cm.controllers;

import io.qameta.allure.Step;
import io.restassured.response.Response;


public class BMWController extends BaseConsentService<BMWController> {

    private final String bmwBasePath = "/bmw";
    private String bmwToken = "5a2404bede9252a1951a5b7783d6f872edc6377ca5faaf1afee055d852e61c92";

    private void withBMWToken(String tokenValue) {
        bmwToken = tokenValue;
    }


    @Step
    public Response pingConsentServiceStatusByBMW() {
        return consentServiceClient(bmwBasePath)
                .header("Authorization", bmwToken)
                .get("/status/ping");
    }

    @Step
    public Response setClearanceStatusByBMW(String clearanceId, String consentState) {
        return consentServiceClient(bmwBasePath)
                .header("Authorization", bmwToken)
                .put("clearances/{clearanceId}/status/{clearanceStatus}", clearanceId, consentState);
    }

}
