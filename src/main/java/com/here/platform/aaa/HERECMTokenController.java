package com.here.platform.aaa;

import static io.restassured.RestAssured.given;

import com.here.platform.cm.controllers.UserAccountController;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;


public class HERECMTokenController {

    private final UserAccountController userAccountController = new UserAccountController();

    private MultiValueMap<String, String> fetchQueryParamsFromUrl(String uri) {
        return UriComponentsBuilder.fromUriString(uri).build().getQueryParams();
    }

    public String generateHERECode(String userLogin, String userPass) {
        Response hereLogin = userAccountController.userAccountOauth();
        String authorizeUrl = hereLogin.getHeader("Location");

        String portalUrl = authorizeUrl.substring(0, authorizeUrl.indexOf("/authorize"));

        String nonce = String.valueOf(System.currentTimeMillis());

        String clientId = fetchQueryParamsFromUrl(authorizeUrl).getFirst("client_id");

        String signInWithPassword = portalUrl + "/api/account/sign-in-with-password";

        Response authorizeResp = given()
                .noFilters()
                .param("nonce", nonce)
                .redirects().follow(false)
                .get(authorizeUrl)
                .then()
                .extract().response();
        Cookies coo = authorizeResp.getDetailedCookies();
        String location = authorizeResp.getHeader("Location");

        Response signInResp = given()
                .noFilters()
                .cookies(coo)
                .get(portalUrl + location)
                .then()
                .extract().response();
        Cookies coo1 = signInResp.getDetailedCookies();

        String body = signInResp.asString();
        String csrfToken = body.substring(body.indexOf("csrf: \""), body.indexOf("\",\n"
                + "            terms: {")).replace("csrf: \"", "");

        Response withPassResult = given()
                .noFilters()
                .cookies(coo1)
                .contentType(ContentType.JSON)
                .config(RestAssured.config().encoderConfig(EncoderConfig.encoderConfig()
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .headers(
                        "Accept", "application/json",
                        "Accept-Encoding", "gzip, deflate, br",
                        "Accept-Language", "en-US,en;q=0.9,ru;q=0.8",
                        "Connection", "keep-alive",
                        "Cache-Control", "no-cache",
                        "Pragma", "no-cache",
                        "x-client", clientId,
                        "x-csrf-token", csrfToken,
                        "x-oidc", "true",
                        "x-sdk", "true",
                        "x-uri", "null",
                        "x-realm", "here",
                        "Sec-Fetch-Dest", "empty",
                        "Sec-Fetch-Mode", "cors",
                        "Sec-Fetch-Site", "same-origin"
                )
                .body("{\"realm\":\"here\",\"email\":\"" + userLogin + "\",\"password\":\"" + userPass
                        + "\",\"rememberMe\":false}")
                .post(signInWithPassword)
                .then()
                .extract().response();

        Cookies coo2 = withPassResult.getDetailedCookies();
        Response sign2Result = given()
                .noFilters()
                .param("scope", "openid%20email%20phone%20profile%20readwrite%3Aha")
                .redirects().follow(false)
                .urlEncodingEnabled(false)
                .cookies(coo2)
                .get(authorizeUrl)
                .then()
                .extract().response();

        String callBackUrl = sign2Result.getHeader("Location");

        return fetchQueryParamsFromUrl(callBackUrl).getFirst("code");
    }

    private String generateCMToken(String authCode) {
        Response userSignIn = userAccountController.userAccountSignIn(authCode);
        return "Bearer " + userSignIn.jsonPath().get("cm_token");
    }

    public String loginAndGenerateCMToken(String userLogin, String userPass) {
        String authCode = generateHERECode(userLogin, userPass);
        return generateCMToken(authCode);
    }

}
