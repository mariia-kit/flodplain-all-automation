package com.here.platform.cm.controllers;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.Map;


public class AccessTokenController extends BaseConsentService<AccessTokenController> {

    @Step("Get access token for consent request id: '{consentRequestId}, VIN: '{vin}', consumerId: '{consumerId}'")
    public Response getAccessToken(String consentRequestId, String vin, String consumerId) {
        final var accessTokenQueryParams = Map.of(
                "consentRequestId", consentRequestId,
                "vin", vin,
                "consumerId", consumerId
        );

        return consentServiceClient("/accessTokens")
                .queryParams(accessTokenQueryParams)
                .when().get();
    }

}
