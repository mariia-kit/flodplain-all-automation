package com.here.platform.dataProviders.daimler;

import static io.restassured.RestAssured.given;

import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainers;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Objects;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;


@Log
public class DaimlerTokenController {

    private final static String
            CALLBACK_URL = ConsentPageUrl.getDaimlerCallbackUrl(),
            MERSEDES_API_URL = "https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2",
            LOGIN_MERSEDES_WL_URL = "https://login.secure.mercedes-benz.com/wl";

    private final DataSubjects targetVehicle;
    private final ConsentRequestContainers container;
    private Cookies mercedesCookies;
    private String sessionId, sessionData;

    public DaimlerTokenController(String targetVehicle, ConsentRequestContainers container) {
        this.targetVehicle = DataSubjects.getByVin(targetVehicle);
        this.container = container;
    }

    public String generateAuthorizationCode() {
        authoriseClient();
        loginUser();
        loginConsent();

        return authoriseConsentAndFetchAuthorizationCode();
    }

    private void authoriseClient() {
        var authorizeResponse = given()
                .baseUri(MERSEDES_API_URL)
                .basePath("authorize")
                .params(
                        "client_id", container.clientId,
                        "response_type", "code",
                        "scope", container.scopeValue,
                        "redirect_uri", CALLBACK_URL,
                        "prompt", "consent login"
                )
                .redirects().follow(false)
                .when().get()
                .then().extract().response();
        mercedesCookies = authorizeResponse.detailedCookies();

        String locationHeaderValue = authorizeResponse.getHeader("Location");
        sessionId = fetchQueryParamsFromUrl(locationHeaderValue).getFirst("sessionID");
        sessionData = fetchQueryParamsFromUrl(locationHeaderValue).getFirst("sessionData");
    }

    private void loginUser() {
        var loginResponse = given()
                .baseUri(LOGIN_MERSEDES_WL_URL)
                .basePath("/login")
                .params(
                        "SMAUTHREASON", "", "target", "", "acr_values", "", "t", "",
                        "sessionID", sessionId,
                        "sessionData", sessionData,
                        "username", targetVehicle.getUserName(),
                        "password", targetVehicle.getPass(),
                        "app-id", "ONEAPI.PROD", "lang", "en_US")
                .cookies(mercedesCookies)
                .when().post()
                .then().extract().response();

        mercedesCookies = loginResponse.detailedCookies();
    }

    private void loginConsent() {
        var scopes = new HashMap<String, String>();
        for (String scopeItem : container.scopeValue.split(" ")) {
            scopes.put(String.format("scope:%s", scopeItem), "on");
        }

        var consentResponse = given()
                .baseUri(LOGIN_MERSEDES_WL_URL)
                .basePath("/consent")
                .formParams(
                        "sessionData", sessionData, "sessionID", sessionId,
                        "app-id", "ONEAPI.PROD", "lang", "en_US")
                .formParams(scopes)
                .cookies(mercedesCookies)
                .redirects().follow(false)
                .when().post()
                .then().extract().response();
        mercedesCookies = consentResponse.detailedCookies();
        sessionData = fetchSessionDataFromHtml(consentResponse);
    }

    private String authoriseConsentAndFetchAuthorizationCode() {
        Response consentCodeCall = given()
                .baseUri(MERSEDES_API_URL)
                .basePath("/authorize/consent")
                .formParams(
                        "action", "Grant",
                        "sessionID", sessionId,
                        "sessionData", sessionData
                )
                .cookies(mercedesCookies)
                .redirects().follow(false)
                .when().post()
                .then()
                .extract().response();

        MultiValueMap<String, String> locationQueryParams = null;
        try {
            locationQueryParams = fetchQueryParamsFromUrl(consentCodeCall.getHeader("Location"));
        } catch (IllegalArgumentException e) {
            log.info("Daimler response:");
            consentCodeCall.prettyPrint();
            e.printStackTrace();
        }
        return Objects.requireNonNull(locationQueryParams).getFirst("code");
    }

    private MultiValueMap<String, String> fetchQueryParamsFromUrl(String uri) {
        return UriComponentsBuilder.fromUriString(uri).build().getQueryParams();
    }

    private String fetchSessionDataFromHtml(Response response) {
        return StringUtils.substringBetween(response.asString(), "name=\"sessionData\" value=", ">")
                .replace("\"", "").replace("'", "");
    }

}
