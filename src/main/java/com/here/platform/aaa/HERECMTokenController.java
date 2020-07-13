package com.here.platform.aaa;

import static io.restassured.RestAssured.given;

import com.here.platform.ns.utils.NS_Config;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;


public class HERECMTokenController {


    public static String generateHERECode(String userLogin, String userPass) {
        String userAccAuthUrl = NS_Config.URL_EXTERNAL_CM + "/oauth";
        Response hereLogin = given()
                .redirects().follow(false)
                .get(userAccAuthUrl)
                .then()
                .extract().response();

        String authorizeUrl = hereLogin.getHeader("Location");

        String portalUrl = authorizeUrl.substring(0, authorizeUrl.indexOf("/authorize"));

        String nonce = String.valueOf(System.currentTimeMillis());

        String clientId = authorizeUrl.substring(authorizeUrl.indexOf("client_id="), authorizeUrl.indexOf("&scope"))
                .replace("client_id=", StringUtils.EMPTY);
        String signInWithPassword = portalUrl + "/api/account/sign-in-with-password";

        Response authorizeResp = given()
                .param("nonce", nonce)
                .redirects().follow(false)
                .get(authorizeUrl)
                .then()
                .extract().response();
        Cookies coo = authorizeResp.getDetailedCookies();
        String location = authorizeResp.getHeader("Location");

        Response signInResp = given()
                .cookies(coo)
                .get(portalUrl + location)
                .then()
                .extract().response();
        Cookies coo1 = signInResp.getDetailedCookies();

        String body = signInResp.asString();
        String csrfToken = body.substring(body.indexOf("csrf: \""), body.indexOf("\",\n"
                + "            terms: {")).replace("csrf: \"", "");

        Response withPassResult = given()
                .cookies(coo1)
                .contentType(ContentType.JSON)
                .config(RestAssured.config().encoderConfig(EncoderConfig.encoderConfig()
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .headers(
                        "Accept", "application/json",
                        "Accept-Encoding", "gzip, deflate, br",
                        "Accept-Language", "en-US,en;q=0.9,ru;q=0.8",
                        "Connection", "keep-alive",
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
                        + "\",\"rememberMe\":true}")
                .post(signInWithPassword)
                .then().log().all()
                .extract().response();

        Cookies coo2 = withPassResult.getDetailedCookies();
        Response sign2Result = given()
                .param("scope", "openid%20email%20phone%20profile%20readwrite%3Aha")
                .redirects().follow(false)
                .urlEncodingEnabled(false)
                .cookies(coo2)
                .get(authorizeUrl)
                .then().log().all()
                .extract().response();

        String callBackUrl = sign2Result.getHeader("Location");

        String code = callBackUrl.substring(callBackUrl.indexOf("code="), callBackUrl.indexOf("&state="))
                .replace("code=", StringUtils.EMPTY);
        return code;
    }

    private static String generateCMToken(String authCode) {
        String userSignInUrl = NS_Config.URL_EXTERNAL_CM + "/sign-in";
        Response userSignIn = given().log().all()
                .redirects().follow(false)
                .header("Content-Type","application/json")
                .header("Accept", "application/json")
                .body("{\"authorizationCode\": \"" + authCode + "\"}")
                .post(userSignInUrl)
                .then().log().all()
                .extract().response();
        return "Bearer " + userSignIn.jsonPath().get("cm_token");
    }

    public static String loginAndGenerateCMToken(String userLogin, String userPass) {
        String authCode = generateHERECode(userLogin, userPass);
        return generateCMToken(authCode);
    }
}
