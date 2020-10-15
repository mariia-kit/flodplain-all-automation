package com.here.platform.dataProviders.daimler;

import static io.restassured.RestAssured.given;

import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainers;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.java.Log;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;


@Log
@Deprecated //cos of migration daimler tests to UI checks
public class DaimlerTokenController {

    private final static String
            CALLBACK_URL = ConsentPageUrl.getDaimlerCallbackUrl(),
            MERSEDES_API_URL = "https://id.mercedes-benz.com/";

    private final DataSubjects targetVehicle;
    private final ConsentRequestContainers container;
    private Cookies mercedesCookies;
    private String resume, requestInfo;

    public DaimlerTokenController(String targetVehicle, ConsentRequestContainers container) {
        this.targetVehicle = DataSubjects.getByVin(targetVehicle);
        this.container = container;
    }

    public String generateAuthorizationCode() {
        authoriseClient();
        loginUser();
        loginConsent();

        return fetchAuthorizationCode();
    }

    private void authoriseClient() {
        var authorizeResponse = given()
                .baseUri(MERSEDES_API_URL)
                .basePath("/as/authorization.oauth2")
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

        authorizeResponse.prettyPrint();

        String locationHeaderValue = authorizeResponse.getHeader("Location");
        resume = fetchQueryParamsFromUrl(locationHeaderValue).getFirst("resume");
        requestInfo = fetchQueryParamsFromUrl(locationHeaderValue).getFirst("request_info");
    }

    private void loginUser() {
        var loginResponse = given()
                .baseUri(MERSEDES_API_URL)
                .basePath("/ciam/auth/login/pass")
                .params(
                        "username", targetVehicle.getUserName(),
                        "password", targetVehicle.getPass(),
                        "rememberMe", false
                )
                .cookies(mercedesCookies)
                .when().post()
                .then().extract().response();

        mercedesCookies = loginResponse.detailedCookies();
    }

    private void loginConsent() {
        var consentResponse = given()
                .baseUri(MERSEDES_API_URL)
                .basePath("/ciam/auth/consent")
                .cookies(mercedesCookies)
                .redirects().follow(false)
                .body(Map.of("grantedScopes", List.of(container.scopeValue.split(" "))))
                .when().post()
                .then().extract().response();
        mercedesCookies = consentResponse.detailedCookies();
    }

    private String fetchAuthorizationCode() {
        Response consentCodeCall = given()
                .baseUri(MERSEDES_API_URL)
                .basePath(resume)
                .formParams(
                        "token", requestInfo
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

}
