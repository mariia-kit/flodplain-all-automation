package com.here.platform.aaa;

import static io.restassured.RestAssured.given;

import com.here.platform.ns.utils.NS_Config;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class PortalTokenController {

    public static String produceToken(String realm, String login, String pass) {
        String portalUrl = NS_Config.GET_PORTAL_PATH.toString();

        DateFormat dateFormatter = new SimpleDateFormat("E MMM dd yyyy HH:mm:ss", Locale.ENGLISH);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT+02:00"));
        Date today = Calendar.getInstance().getTime();
        String nonce = dateFormatter.format(today) + " GMT+0200 (Eastern European Standard Time)";

        String clientId =
                "prod".equalsIgnoreCase(System.getProperty("env")) ? "YQijV3hAPdxySAVtE6ZT" : "TlZSbQzENfNkUFrOXh8Oag";
        String signInWithPassword = portalUrl + "/api/account/sign-in-with-password";
        String authorizeUrl = portalUrl + "/authorize?"
                + "client_id=" + clientId + "&"
                + "response_type=code&"
                + "scope=openid%20email%20phone%20profile%20readwrite%3Aha&"
                + "prompt=login&"
                + "no-sign-up=true&"
                + "realm-input=true&"
                + "sign-in-template=olp&"
                + "sign-in-screen-config=password";

        Response authorizeResp = given()
                .when()
                .param("nonce", nonce)
                .redirects().follow(false)
                .get(authorizeUrl);
        Cookies coo = authorizeResp.getDetailedCookies();
        String location = authorizeResp.getHeader("Location");

        Response signInResp = given()
                .when()
                .cookies(coo)
                .get(portalUrl + location);
        Cookies coo1 = signInResp.getDetailedCookies();

        String body = signInResp.getBody().asString();
        String csrfToken = body.substring(body.indexOf("csrf: \""), body.indexOf("\",\n"
                + "            terms: {")).replace("csrf: \"", "");

        Response withPassResult = given()
                .cookies(coo1)
                .contentType(ContentType.JSON)
                .config(RestAssured
                        .config().encoderConfig(EncoderConfig.encoderConfig()
                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .header("Accept", "application/json")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Accept-Language", "en-US,en;q=0.9,ru;q=0.8")
                .header("Connection", "keep-alive")
                .header("x-client", clientId)
                .header("x-csrf-token", csrfToken)
                .header("x-oidc", "true")
                .header("x-sdk", "true")
                .header("x-uri", "null")
                .header("x-realm", "here")
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .body("{\"realm\":\"" + realm + "\",\"email\":\"" + login + "\",\"password\":\"" + pass
                        + "\",\"rememberMe\":true}")
                .post(signInWithPassword);

        return withPassResult.jsonPath().get("accessToken");
    }

}
