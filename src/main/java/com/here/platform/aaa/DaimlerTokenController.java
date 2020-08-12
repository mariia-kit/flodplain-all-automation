package com.here.platform.aaa;

import static io.restassured.RestAssured.given;

import io.restassured.http.Cookie;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;


public class DaimlerTokenController {

    public static String produceConsentAuthCode(String clientId, String clientSecret, String login, String pass,
            String callbackUrl) {

        String scope = "mb:user:pool:reader%20mb:vehicle:status:general";
        String authorize = "https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/authorize";
        Response authResp = given()
                .header("Accept",
                        "text/html,applicat\"prompt\", \"consent%20login\")\n"
                                + "        .urlEncodingEnabled(false)\n"
                                + "        .redirects().follow(false)ion/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Accept-Language", "en-US,en;q=0.9,ru;q=0.8")
                .header("Upgrade-Insecure-Requests", "1")
                .param("client_id", clientId)
                .param("response_type", "code")
                .param("scope", scope)
                .param("redirect_uri", callbackUrl)
                .param("prompt", "consent%20login")
                .urlEncodingEnabled(false)
                .redirects().follow(false)
                .when()
                .get(authorize);
        Cookies coo = authResp.detailedCookies();
        String location = authResp.getHeader("Location");

        Map<String, String> query_pairs = parseQueryPairs(location);
        String sessionID = query_pairs.get("sessionID");
        String sessionData = query_pairs.get("sessionData");

        String loginDaimlerDo = "https://login.secure.mercedes-benz.com/wl/login";

        Response loginCall = given()
                .header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .param("SMAUTHREASON", "")
                .param("target", "")
                .param("acr_values", "")
                .param("t", "")
                .param("sessionID", sessionID)
                .param("sessionData", sessionData)
                .param("app-id", "ONEAPI.PROD")
                .param("username", login)
                .param("password", pass)
                .param("lang", "en_US")
                .cookies(coo).
                        when().post(loginDaimlerDo).
                        then()
                .extract().response();

        coo = mergeCookies(coo, loginCall.detailedCookies());

        String consentDo = "https://login.secure.mercedes-benz.com/wl/consent";

        Response consentCall = given()
                .header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cache-Control", "max-age=0")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Accept-Languag", "en-US,en;q=0.9,ru;q=0.8")
                .header("Cache-Control", "max-age=0")
                .formParam("sessionData", sessionData)
                .formParam("sessionID", sessionID)
                .formParam("app-id", "ONEAPI.PROD")
                .formParam("scope:mb:vehicle:status:general", "on")
                .formParam("scope:mb:user:pool:reader", "on")
                .formParam("lang", "en_US")
                .cookies(coo)
                .redirects().follow(false)
                .when().post(consentDo);
        coo = mergeCookies(coo, loginCall.detailedCookies());
        String newData = StringUtils.substringBetween(consentCall.getBody().prettyPrint(),
                "name=\"sessionData\" value=\"", "\"/>");

        String authoriseConsent = "https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/authorize/consent";

        Response consentCodeCall = given()
                .header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("action", "Grant")
                .formParam("sessionID", sessionID)
                .formParam("sessionData", newData)
                .cookies(coo)
                .redirects().follow(false)
                .when().post(authoriseConsent);

        return consentCodeCall.getHeader("Location")
                .replace(callbackUrl + "?code=", StringUtils.EMPTY);
    }

    public static String createDaimlerToken(String authCode, String clientId, String clientSecret, String callbackUrl) {
        String url = "https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/token";

        Response temp = given()
                .auth()
                .preemptive().basic(clientId, clientSecret)
                .header("content-type", "application/x-www-form-urlencoded")
                .formParam("grant_type", "authorization_code")
                .formParam("code", authCode)
                .formParam("redirect_uri", callbackUrl)
                .when().post(url)
                .then()
                .statusCode(200)
                .extract().response();
        return temp.jsonPath().get("access_token") + ":" + temp.jsonPath().get("refresh_token");
    }

    private static Cookies mergeCookies(Cookies oldCookies, Cookies newCookies) {
        List<Cookie> coo1 = oldCookies.asList();
        List<Cookie> coo2 = newCookies.asList();
        List<Cookie> coo23 = new ArrayList<>();
        coo23.addAll(coo1);
        coo23.addAll(coo2);
        return new Cookies(coo23);
    }

    private static Map<String, String> parseQueryPairs(String location) {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String[] pairs = location.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(
                    URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8),
                    URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8)
            );
        }
        return query_pairs;
    }

}
