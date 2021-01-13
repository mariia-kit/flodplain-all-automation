package com.here.platform.cm.controllers;

import com.here.platform.cm.rest.model.HereAccountRequestTokenData;
import com.here.platform.common.strings.VIN;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;


//todo implement UserCleanUpExtension
public class UserAccountController extends BaseConsentService<UserAccountController> {

    private final String userBasePath = "/user";

    @Step("Redirect user to OAUTH page")
    public Response userAccountOauth() {
        return consentServiceClient(StringUtils.EMPTY)
                .noFilters()
                .redirects().follow(false)
                .get("/oauth/sign-in");
    }

    @Step("Sign in user to CM by authorization code: '{authorizationCode}'")
    public Response userAccountSignIn(String authorizationCode) {
        return consentServiceClient(StringUtils.EMPTY)
                //.noFilters()
                .body(new HereAccountRequestTokenData().authorizationCode(authorizationCode))
                .redirects().follow(false)
                .post("/sign-in");
    }

    @Step("Get CM user account info with CM token")
    public Response userAccountGetInfo(String privateBearerToken) {
        return consentServiceClient(userBasePath)
                .header("Authorization", privateBearerToken)
                .get("/info");
    }

    @Step("Add VIN '{vin}' to CM user account")
    public Response attachVinToUserAccount(String vin, String privateBearerToken) {
        return consentServiceClient(userBasePath)
                .header("Authorization", privateBearerToken)
                .put("/vin/{vin}", vin);
    }

    @Step("Get consents for user with CM token, crid and state: '{cridAndState}'")
    public Response getConsentsForUser(String privateBearerToken, Map<String, Object> cridAndState) {
        return consentServiceClient(userBasePath)
                .header("Authorization", privateBearerToken)
                .params(cridAndState)
                .get("/consents");
    }

    @Step("Delete VIN '{vin}' for user with CM token")
    public Response deleteVINForUser(String vin, String privateBearerToken) {
        var vinHash = new VIN(vin).hashed();
        return consentServiceClient(userBasePath)
                .header("Authorization", privateBearerToken)
                .delete("/vin/{vinHash}", vinHash);
    }

    @Step("HERE callback on user account delete.")
    public Response userAccountDeleteCallback(String actionWithSignature) {
        return consentServiceClient(userBasePath)
                .contentType(ContentType.TEXT)
                .body(actionWithSignature)
                .post("/ha/event/callback");
    }
}
