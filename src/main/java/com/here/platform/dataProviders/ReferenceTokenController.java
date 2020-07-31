package com.here.platform.dataProviders;

import static io.restassured.RestAssured.given;

import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainers;
import io.restassured.http.Cookies;
import org.apache.commons.lang3.StringUtils;


public class ReferenceTokenController {

    private final static String
            CALLBACK_URL =
            ConsentPageUrl.getEnvUrlRoot() + "oauth2/referenceProvider/auth/callback",
            REFERENCE_URL = "https://data-reference-provider-dev.ns.api.platform.in.here.com/",
            REFERENCE_AUTH_URL = REFERENCE_URL + "auth/oauth/v2",
            CLIENT_ID = "consent_mediator",
            CLIENT_SECRET = "123";

    private Cookies mercedesCookies;
    private String sessionId, sessionData;
    private final DataSubjects targetVehicle;
    private final ConsentRequestContainers container;


    public ReferenceTokenController(String targetVehicle, ConsentRequestContainers container) {
        this.targetVehicle = DataSubjects.getByVin(targetVehicle);
        this.container = container;
    }

    public String generateAuthorizationCode() {
        String referenceConsentId = authoriseClient();
        return approveConsentReferenceSide(referenceConsentId, targetVehicle.vin);

    }

    private String authoriseClient() {
        var authorizeResponse = given()
                .baseUri(REFERENCE_AUTH_URL)
                .basePath("authorize")
                .params(
                        "client_id", CLIENT_ID,
                        "response_type", "code",
                        "scope", container.scopeValue,
                        "redirect_uri", CALLBACK_URL,
                        "prompt", "login,consent"
                )
                .redirects().follow(false)
                .when().get()
                .then().extract().response();

        return StringUtils.substringBetween(authorizeResponse.asString(),
                "consent?consent_id=", "\">");
    }


    private String approveConsentReferenceSide(String referenceConsentId, String vin) {
        var approveResponse = given()
                .baseUri(REFERENCE_URL)
                .basePath("consent")
                .params(
                        "consent_id", referenceConsentId,
                        "vin", vin
                )
                .when().post()
                .then().extract().response();

        return approveResponse.getHeader("Location")
                .replace(CALLBACK_URL + "?code=", StringUtils.EMPTY);
    }

}
