package com.here.platform.aaa;

import static io.restassured.RestAssured.given;

import com.here.platform.common.config.Conf;
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
import org.apache.commons.lang3.StringUtils;


public class PortalTokenController {

    public static String produceToken(String realm, String login, String pass) {
        String portalUrl = Conf.ns().getPortalUrl();

        DateFormat dateFormatter = new SimpleDateFormat("E MMM dd yyyy HH:mm:ss", Locale.ENGLISH);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT+02:00"));
        Date today = Calendar.getInstance().getTime();
        String nonce = dateFormatter.format(today) + " GMT+0200 (Eastern European Standard Time)";

        String clientId =
                "prod".equalsIgnoreCase(System.getProperty("env")) ? "YQijV3hAPdxySAVtE6ZT" : "TlZSbQzENfNkUFrOXh8Oag";
        String redirectUrl =
                "prod".equalsIgnoreCase(System.getProperty("env")) ? "https%3A%2F%2Fplatform.here.com%2F" : "https%3A%2F%2Fplatform.in.here.com%2F";
        String signInWithPassword = portalUrl + "/api/account/sign-in-with-password";
        String authorizeUrl = portalUrl + "/authorize?"
                + "client_id=" + clientId + "&"
                + "prompt=login&"
                + "response_type=code&"
                + "scope=openid%20email%20phone%20profile%20readwrite%3Aha&"
                + "redirect_uri=" + redirectUrl + "authHandler&"
                + "state=%7B%22redirectUri%22%3A%22" + redirectUrl + "authHandler%22%2C%22"
                + "redirect%22%3A%22https%253A%252F%252Fplatform.in.here.com%252Fmarketplace%252F%22%7D&"
                + "no-sign-up=true&realm-input=true&sign-in-template=olp&sign-in-screen-config=password";
        Response authorizeResp = given()
                .when()
                .param("nonce", nonce)
                .redirects().follow(false)
                .get(authorizeUrl);
        Cookies coo = authorizeResp.getDetailedCookies();
        String location = authorizeResp.getHeader("Location");

        if (StringUtils.isEmpty(location)) {
            throw new RuntimeException("Error during authorization on Here portal." + authorizeResp.getBody().print());
        }

        Response signInResp = given()
                .when()
                .cookies(coo)
                .urlEncodingEnabled(false)
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
