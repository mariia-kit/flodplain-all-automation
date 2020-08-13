package com.here.platform.aaa;

import static io.restassured.RestAssured.given;

import com.here.platform.ns.utils.NS_Config;
import io.restassured.response.Response;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;


public class ReferenceTokenController {

    private static final String CALLBACK_URL = "https://dev-web.consent.api.platform.in.here.com/oauth2/referenceProvider/auth/callback";

    public static String produceConsentAuthCode(String vin, String scope) {

        String clientId = "consent_mediator";
        String clientSecret = "123";
        String login = new String(Base64.getDecoder().decode(NS_Config.DAIMLER_API_LOGIN.toString()));
        String pass = new String(Base64.getDecoder().decode(NS_Config.DAIMLER_API_PASS.toString()));

        String authorize = NS_Config.REFERENCE_J_PROV_URL.toString() + "/auth/oauth/v2/authorize";
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

        String makeConsent = NS_Config.REFERENCE_J_PROV_URL.toString() + "/consent";
        Response consentCall = given()
                .param("consent_id", consentId)
                .param("vin", vin)
                .when().post(makeConsent);

        Assertions.assertTrue(consentCall != null && !StringUtils.isEmpty(consentCall.getHeader("Location")),
                "Can't allocate auth code for reference provider!");
        return consentCall.getHeader("Location")
                .replace(CALLBACK_URL + "?code=", StringUtils.EMPTY);
    }

    /*
    {
    "providerId": "exelsior",
    "consumerId": "olp-here-mrkt-cons-1",
    "container": "pay_as_you_drive",
    "clientId": "consent_mediator",
    "clientSecret": "123",
    "redirectUri": "https://dev-web.consent.api.platform.in.here.com/oauth2/referenceProvider/auth/callback"
  }

      {
    "providerId": "exelsior",
    "consumerId": "olp-here-mrkt-cons-1",
    "container": "pay_as_you_drive",
    "clientId": "consent_mediator",
    "clientSecret": "123",
    "redirectUri": "https://sit-web.consent.api.platform.in.here.com/oauth2/referenceProvider/auth/callback"
  }

  {
    "id": "exelsior",
    "name": "referenceProvider",
    "properties": {
      "authUrl": "https://reference-data-provider.ost.solo-experiments.com/auth/oauth/v2/authorize",
      "responseType": "code",
      "prompt": "consent,login",
      "tokenUrl": "https://reference-data-provider.ost.solo-experiments.com/auth/oauth/v2/token"
    }
}
     */
}
