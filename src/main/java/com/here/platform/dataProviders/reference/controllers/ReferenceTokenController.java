package com.here.platform.dataProviders.reference.controllers;

import static com.here.platform.common.strings.SBB.sbb;
import static io.restassured.RestAssured.given;

import com.here.platform.common.config.Conf;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;


public class ReferenceTokenController {

    private static final String CALLBACK_URL = Conf.ns().getReferenceApp().getCallBackUrl();

    @SneakyThrows
    public static String produceConsentAuthCode(String vin, String scope) {
        String clientId = Conf.ns().getReferenceApp().getClientId();
        String consentId = createConsent(clientId, scope);
        Response consentCall = approve(consentId, vin);

        Assertions.assertTrue(consentCall != null && !StringUtils.isEmpty(consentCall.getHeader("Location")),
                sbb("Can't allocate auth code for reference provider!").append(consentCall.getStatusCode()).bld());
        return consentCall.getHeader("Location")
                .replace(CALLBACK_URL + "?code=", StringUtils.EMPTY);
    }

    private static String createConsent(String clientId, String scope) {
        String authorize = Conf.ns().getRefProviderUrl() + "/auth/oauth/v2/authorize";
        Response authResp = given()
                .param("client_id", clientId)
                .param("response_type", "code")
                .param("scope", scope)
                .param("redirect_uri", CALLBACK_URL)
                .param("prompt", "consent,login")
                .urlEncodingEnabled(false)
                .redirects().follow(false)
                .when().get(authorize);
        String consentId = StringUtils.substringBetween(authResp.asString(), "consent?consent_id=", "\">");
        return consentId;
    }

    private static Response approve(String consentId, String vin) {
        String makeConsent = Conf.ns().getRefProviderUrl() + "/consent";
        return given()
                .param("consent_id", consentId)
                .param("vin", vin)
                .when().post(makeConsent);
    }

}
