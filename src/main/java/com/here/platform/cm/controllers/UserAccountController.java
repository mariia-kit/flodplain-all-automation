package com.here.platform.cm.controllers;

import com.here.platform.cm.rest.model.HereAccountRequestTokenData;
import com.here.platform.common.strings.VIN;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;


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

    @SneakyThrows
    @Step("Sign in user to CM by authorization code: '{authorizationCode}'")
    public Response userAccountSignIn(String authorizationCode) {
        Response resp = consentServiceClient(StringUtils.EMPTY)
                .body(new HereAccountRequestTokenData().authorizationCode(authorizationCode))
                .redirects().follow(false)
                .post("/sign-in");
        if (resp.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
            Thread.sleep(1000);
            return consentServiceClient(StringUtils.EMPTY)
                    .body(new HereAccountRequestTokenData().authorizationCode(authorizationCode))
                    .redirects().follow(false)
                    .post("/sign-in");
        }
        return resp;
    }

    public Response userAccountGetInfo(String privateBearerToken) {
        return consentServiceClient(userBasePath)
                .noFilters()
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
