package com.here.platform.aaa;

import static io.restassured.RestAssured.given;

import com.here.platform.common.config.Conf;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;


public class ReferenceTokenController {

    private static final String CALLBACK_URL = Conf.ns().getReferenceApp().getCallBackUrl();

    public static String produceConsentAuthCode(String vin, String scope) {

        String clientId = Conf.ns().getReferenceApp().getClientId();

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
        String consentId = StringUtils.substringBetween(authResp.asString(),
                "consent?consent_id=", "\">");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String makeConsent = Conf.ns().getRefProviderUrl() + "/consent";
        Response consentCall = given()
                .param("consent_id", consentId)
                .param("vin", vin)
                .when().post(makeConsent);

        Assertions.assertTrue(consentCall != null && !StringUtils.isEmpty(consentCall.getHeader("Location")),
                "Can't allocate auth code for reference provider!");
        return consentCall.getHeader("Location")
                .replace(CALLBACK_URL + "?code=", StringUtils.EMPTY);
    }

}
