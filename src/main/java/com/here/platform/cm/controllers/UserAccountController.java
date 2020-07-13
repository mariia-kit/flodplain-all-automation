package com.here.platform.cm.controllers;

import com.here.platform.cm.rest.model.HereAccountRequestTokenData;
import com.here.platform.common.VIN;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;


public class UserAccountController extends BaseConsentService {

    private final String userBasePath = "/user";

    @Step
    public Response userAccountOauth() {
        return consentServiceClient(StringUtils.EMPTY)
                .noFilters()
                .redirects().follow(false)
                .get("/oauth");
    }

    @Step
    public Response userAccountSignIn(String authorizationCode) {
        return consentServiceClient(StringUtils.EMPTY)
                .noFilters()
                .body(new HereAccountRequestTokenData().authorizationCode(authorizationCode))
                .redirects().follow(false)
                .post("/sign-in");
    }

    @Step
    public Response userAccountGetInfo(String privateBearerToken) {
        return consentServiceClient(userBasePath)
                .header("Authorization", privateBearerToken)
                .get("/info");
    }

    @Step
    public Response attachConsumerToUserAccount(String consentRequestId, String privateBearerToken) {
        return consentServiceClient(userBasePath)
                .header("Authorization", privateBearerToken)
                .put("/consumer/consentRequest/{consentRequestId}", consentRequestId);
    }

    @Step
    public Response attachVinToUserAccount(String vin, String privateBearerToken) {
        return consentServiceClient(userBasePath)
                .header("Authorization", privateBearerToken)
                .put("/vin/{vin}", vin);
    }

    @Step
    public Response getConsentsForUser(String privateBearerToken, Map<String, Object> cridAndState) {
        return consentServiceClient(userBasePath)
                .header("Authorization", privateBearerToken)
                .params(cridAndState)
                .get("/consents");
    }

    @Step
    public Response deleteVINForUser(String vin, String privateBearerToken) {
        var vinHash = new VIN(vin).hashed();
        return consentServiceClient(userBasePath)
                .header("Authorization", privateBearerToken)
                .delete("/vin/{vinHash}", vinHash);
    }

    @Step
    public Response deleteConsumerForUser(String consumerId, String privateBearerToken) {
        return consentServiceClient(userBasePath)
                .header("Authorization", privateBearerToken)
                .delete("/consumer/{consumerId}", consumerId);
    }

}
