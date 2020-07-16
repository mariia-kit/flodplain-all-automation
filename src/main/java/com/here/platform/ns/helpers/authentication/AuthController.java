package com.here.platform.ns.helpers.authentication;

import static io.restassured.RestAssured.given;

import com.here.platform.aaa.ApplicationTokenController;
import com.here.platform.aaa.DaimlerTokenController;
import com.here.platform.aaa.HERECMTokenController;
import com.here.platform.aaa.PortalTokenController;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.UserType_NS;
import com.here.platform.ns.utils.NS_Config;
import com.here.platform.ns.utils.PropertiesLoader;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Assertions;


public class AuthController {

    private final static Logger logger = Logger.getLogger(AuthController.class);
    private static String appToken = StringUtils.EMPTY;

    public synchronized static void setUserToken(User user) {
        String token = loadOrGenerate(user, () -> {
            switch (user.getType()) {
                case NS:
                    if ("prod".equalsIgnoreCase(System.getProperty("env"))
                            && user.getType().equals(UserType_NS.NS)) {
                        logger.info("------------- Creating new APP LIKE user token ------------");
                        String host = NS_Config.URL_AUTH.toString() + NS_Config.GET_TOKEN_PATH.toString();
                        String clientIdValue = NS_Config.APP_KEY.toString();
                        String clientSecretValue = NS_Config.APP_SECRET.toString();
                        return ApplicationTokenController.createConsumerAppToken(host, clientIdValue, clientSecretValue);
                    } else {
                        logger.info("------------- Creating new portal user token ------------");
                        return PortalTokenController.produceToken(user.getRealm(), user.getEmail(), user.getPass());
                    }
                case MP:
                    logger.info("------------- Creating new portal user token ------------");
                    return PortalTokenController.produceToken(user.getRealm(), user.getEmail(), user.getPass());
                case CM:
                    logger.info("------------- Creating new CM user token ------------");
                    return HERECMTokenController.loginAndGenerateCMToken(user.getEmail(), user.getPass());
                case APP:
                    logger.info("------------- Creating new APP LIKE user token ------------");
                    String host = NS_Config.URL_AUTH.toString() + NS_Config.GET_TOKEN_PATH.toString();
                    String clientIdValue = NS_Config.APP_KEY.toString();
                    String clientSecretValue = NS_Config.APP_SECRET.toString();
                    return ApplicationTokenController.createConsumerAppToken(host, clientIdValue, clientSecretValue);
                case DAIMLER:
                    logger.info("------------- Creating new Daimler user token ------------");
                    String clientId = PropertiesLoader.getInstance().mainProperties.getProperty("daimler.clientId");
                    String clientSecret = PropertiesLoader.getInstance().mainProperties.getProperty("daimler.clientSecret");
                    String callbackUrl = PropertiesLoader.getInstance().mainProperties.getProperty("daimler.callbackurl");
                    String code = DaimlerTokenController.produceConsentAuthCode(
                            clientId,
                            clientSecret,
                            user.getEmail(), user.getPass(),
                            callbackUrl);
                    return DaimlerTokenController.createDaimlerToken(code, clientId, clientSecret, callbackUrl);
                case AA:
                    logger.info("------------- Creating new AA user token ------------");
                    return ApplicationTokenController.createConsumerAppToken(
                            NS_Config.URL_AUTH.toString() + NS_Config.GET_TOKEN_PATH.toString(),
                            NS_Config.AAA_ID.toString(),
                            NS_Config.AAA_SECRET.toString());
                default:
                    return StringUtils.EMPTY;
            }
        });
        user.setToken(token);
    }

    public static String loadOrGenerate(User user, Supplier<String> supplier) {
        String currentT = PropertiesLoader.getInstance().loadToken(user.getEmail() + "_" + user.getRealm());
        if (StringUtils.isEmpty(currentT)) {
            String token = supplier.get();
            PropertiesLoader.getInstance().saveToken(user.getEmail() + "_" + user.getRealm(), token);
            return token;
        } else {
            return currentT;
        }
    }


//
//    public synchronized static void initUserToken(User user) {
//        logger.info("--------------- Init User token " + user.getEmail() + "_" + user.getRealm() + "------------\n");
//        String currentT = PropertiesLoader.getInstance().loadToken(user.getEmail() + "_" + user.getRealm());
//        if (StringUtils.isEmpty(currentT)) {
//            setUserToken(user);
//            String newToken = user.getToken();
//            PropertiesLoader.getInstance().saveToken(user.getEmail() + "_" + user.getRealm(), newToken);
//        } else {
//            user.setToken(currentT);
//        }
//        logger.info("User " + user.getEmail() + " " + user.getRealm() + " initialized.");
//    }
//
//    public synchronized static Boolean isTokenExpired(String tokenToBeValidated, String email,
//            String realm) {
//        logger.info("--------------- Is token expired for " + email + "------------\n");
//        String getRequestUrl = NS_Config.URL_AUTH.toString() + NS_Config.VALIDATE_TOKEN_PATH.toString();
//        Response r = given()
//                .log().all()
//                .header("Authorization", tokenToBeValidated)
//                .when().get(getRequestUrl)
//                .then()
//                .log().all()
//                .extract().response();
//        logger.info(
//                "--------------- Validating if the current token is valid and has not expired ------------\n");
//        if (r.getStatusCode() == 200 && email.equals(r.getBody().jsonPath().getString("email"))
//                && realm
//                .equals(r.getBody().jsonPath().getString("realm"))) {
//            logger.info("--------- The actual token is valid for user : " + r.getBody().jsonPath()
//                    .get("firstname") + " ------------ \n");
//            return false;
//        } else {
//            logger.info(r.getBody().jsonPath().get("message"));
//            return true;
//        }
//    }
//
//    @Step("Init new token for realm {0} with request {1}")
//    public synchronized static String createNewXHAToken(User user) {
//        String body = "{\n" +
//                "  \"grantType\": \"password\",\n" +
//                "  \"email\": \"" + user.getEmail() + "\",\n" +
//                "  \"password\": \"" + user.getPass() + "\",\n" +
//                "  \"countryCode\": \"USA\",\n" +
//                "  \"language\": \"en\",\n" +
//                "  \"clientId\": \"" + user.getClientId() + "\",\n" +
//                "  \"clientSecret\": \"" + user.getClientSecret() + "\",\n" +
//                "  \"tokenFormat\": \"hN\"\n" +
//                "}";
//        Response tokenResponse = given().log().all()
//                .contentType(ContentType.JSON)
//                .header("x-ha-realm", user.getRealm())
//                .when().body(body)
//                .post(NS_Config.URL_AUTH.toString() + NS_Config.GET_TOKEN_PATH.toString());
//        Assertions.assertEquals(200, tokenResponse.getStatusCode(),
//                "Something went wrong trying to create " +
//                        "a new user token, see: \n" +
//                        tokenResponse.getHeaders() + " \n" + tokenResponse.getBody()
//                        .prettyPrint());
//        return tokenResponse.jsonPath().get("accessToken");
//    }
//
//
//
//    public static String initAAToken(User user) {
//        String currentT = PropertiesLoader.getInstance().loadToken(user.getEmail());
//        if (StringUtils.isEmpty(currentT)) {
//            String newToken = "Bearer " + createAAAtoken();
//            PropertiesLoader.getInstance().saveToken(user.getEmail(), newToken);
//            return newToken;
//        } else {
//            return currentT;
//        }
//    }
//
//    public synchronized static String createAAAtoken() {
//        String url = NS_Config.URL_AUTH.toString() + NS_Config.GET_TOKEN_PATH.toString();
//        String consumerKey = NS_Config.AAA_ID.toString();
//        String consumerSecretToken = NS_Config.AAA_SECRET.toString();
//
//        Response temp = given().auth().oauth(consumerKey,
//                consumerSecretToken, "", "").log().all().
//                contentType(ContentType.JSON).
//                when().body("{  \"grantType\": \"client_credentials\" }").post(url).
//                then().log().all()
//                .statusCode(200)
//                .extract().response();
//        return temp.jsonPath().get("accessToken");
//    }
//
//    public static String initDaimlerToken(User user) {
//        String currentT = PropertiesLoader.getInstance().loadToken(user.getEmail());
//        if (StringUtils.isEmpty(currentT)) {
//            String newToken = createDaimlerToken();
//            PropertiesLoader.getInstance().saveToken(user.getEmail(), newToken);
//            return newToken;
//        } else {
//            return currentT;
//        }
//    }
//
//    public static String produceDaimlerAuthCode() {
//        String auth = "https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/authorize";
//        String loginUrl = "https://login.secure.mercedes-benz.com/wl/login";
//        String consentUrl = "https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/authorize/consent";
//        String clientId = "88440bf1-2fff-42b6-8f99-0510b6b5e6f8";
//        String clientSecret = "2d839912-c5e6-4cfb-8543-9a1bed38efe6";
//        String scope = "mb:user:pool:reader mb:vehicle:status:general";
//        String login = new String(Base64.getDecoder().decode(NS_Config.DAIMLER_API_LOGIN.toString()));
//        String pass = new String(Base64.getDecoder().decode(NS_Config.DAIMLER_API_PASS.toString()));
//
//        Response temp1 = given()
//                .auth()
//                .preemptive()
//                .basic(login, pass)
//                .param("client_id", clientId)
//                .param("response_type", "code")
//                .param("scope", scope)
//                .param("redirect_uri", "https://sit-web.consent.api.platform.in.here.com/oauth2/daimler/auth/callback")
//                .log().all().
//                        when().post(auth).
//                        then().log().all()
//                .extract().response();
//        String location = temp1.getHeader("Location");
//        Cookies coo = temp1.detailedCookies();
//        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
//        String[] pairs = location.split("&");
//        for (String pair : pairs) {
//            int idx = pair.indexOf("=");
//            try {
//                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
//                        URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//        }
//        String sessionID = query_pairs.get("sessionID");
//        String sessionData = query_pairs.get("sessionData");
//
//        Response loginCall = given()
//                .header("Accept",
//                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
//                .header("Content-Type", "application/x-www-form-urlencoded")
//                .formParam("SMAUTHREASON", "")
//                .formParam("target", "")
//                .formParam("acr_values", "")
//                .formParam("t", "")
//                .formParam("sessionID", sessionID)
//                .formParam("sessionData", sessionData)
//                .formParam("app-id", "ONEAPI.PROD")
//                .formParam("username", login)
//                .formParam("password", pass)
//                .formParam("lang", "en_US")
//                .log().all().
//                        when().post(loginUrl).
//                        then().log().all()
//                .extract().response();
//        String newData = StringUtils.substringBetween(loginCall.getBody().prettyPrint(),
//                "name=\"sessionData\" value=\"", "\"/>");
//
//        Response consentCall = given()
//                .header("Accept",
//                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
//                .header("Content-Type", "application/x-www-form-urlencoded")
//                .cookies(coo)
//                .log().all().
//                        when()
//                .post(consentUrl + "?action=Grant&sessionID=" + sessionID + "&sessionData="
//                        + newData).
//                        then().log().all()
//                .extract().response();
//
//        return consentCall.getHeader("Location")
//                .replace("https://sit-web.consent.api.platform.in.here.com/oauth2/daimler/auth/callback?code=", StringUtils.EMPTY);
//    }
//
//    public synchronized static String createDaimlerToken() {
//        String url = "https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/token";
//        String clientId = "88440bf1-2fff-42b6-8f99-0510b6b5e6f8";
//        String clientSecret = "2d839912-c5e6-4cfb-8543-9a1bed38efe6";
//
//        String code = produceDaimlerAuthCode();
//
//        Response temp = given()
//                .auth()
//                .preemptive().basic(clientId, clientSecret)
//                .header("content-type", "application/x-www-form-urlencoded")
//                .formParam("grant_type", "authorization_code")
//                .formParam("code", code)
//                .formParam("redirect_uri", "https://sit-web.consent.api.platform.in.here.com/oauth2/daimler/auth/callback")
//                .log().all().
//                        when().post(url).
//                        then().log().all()
//                .statusCode(200)
//                .extract().response();
//        return temp.jsonPath().get("access_token") + ":" + temp.jsonPath().get("refresh_token");
//    }
//
//    public Boolean isAppTokenExpired(String appToken) {
//        logger.info("------------ Validating Super User Token ---------------");
//        String getRequestUrl = NS_Config.URL_AUTH.toString() + NS_Config.VALIDATE_ACCESS_TOKEN;
//        Response r = given().
//                contentType(ContentType.JSON).
//                header("Authorization", "Bearer " + appToken).
//                when().body("{\n" +
//                "  \"token\": \"" + appToken + "\"\n" +
//                "}").
//                post(getRequestUrl).
//                then().extract().response();
//        logger.info(
//                "------------ Validating if the current token is valid and has not expired ----------\n");
//        if (r.getStatusCode() == 200) {
//            logger.info(
//                    "--------- The actual token is Valid realm : " + r.getBody().jsonPath()
//                            .get("realm")
//                            + " ---------");
//            return false;
//        } else {
//            logger.info(r.getBody().jsonPath().get("message"));
//            return true;
//        }
//    }

}
