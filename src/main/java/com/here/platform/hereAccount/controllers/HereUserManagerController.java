package com.here.platform.hereAccount.controllers;


import static com.here.platform.common.strings.SBB.sbb;

import com.github.javafaker.Faker;
import com.here.platform.cm.steps.api.StatusCodeExpects;
import com.here.platform.common.config.Conf;
import io.qameta.allure.Step;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import java.util.List;
import lombok.Data;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;


//todo implent extension to use for DEV and SIT environment
public class HereUserManagerController extends BaseHereAccountController {

    @Step("Create HERE user {hereUser.email}:{hereUser.password}")
    public void createHereUser(HereUser hereUser) {
        Assertions.assertEquals("here", hereUser.getRealm(),
                "Not all realms allow to create users without invite. Please, use 'here'");
        createHereAccount(hereUser);
        String reAcceptanceToken = getHereCurrentToken(hereUser);
        acceptHereTerms(hereUser, reAcceptanceToken);
    }

    @Step("Delete HERE user {hereUser.email}")
    public void deleteHereUser(HereUser hereUser) {
        String token = getHereCurrentToken(hereUser);
        deleteHereAccount(sbb("Bearer").w().append(token).bld());
    }

    public String createHereAccount(HereUser hereUser) {
        var create = hereAccountClient("/user")
                .body(new CreateHereUserRequest(hereUser))
                .post();
        return StatusCodeExpects.expectCREATEDStatusCode(create)
                .body().jsonPath().get("userId");
    }

    public void deleteHereAccount(String bearerToken) {
        Response authorization = hereAccountClient("/user/me")
                .header("Authorization", bearerToken)
                .delete();
        StatusCodeExpects.expectNOCONSTENTStatusCode(authorization);
    }

    public String getHereCurrentToken(HereUser hereUser) {
        Response tokenResponse = hereAccountClient(Conf.ns().getAuthUrlGetToken())
                .header("x-ha-realm", hereUser.getRealm())
                .when().body(new GetCurrentTokenRequest(hereUser))
                .post();
        if (tokenResponse.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED) {
            return tokenResponse.jsonPath().get("termsReacceptanceToken");
        } else if (tokenResponse.getStatusCode() == HttpStatus.SC_OK) {
            return tokenResponse.jsonPath().get("accessToken");
        } else {
            Assertions.fail(
                    sbb("Error receiving token or acceptance token!").w().append(tokenResponse.asString()).bld()
            );
            return null;
        }
    }

    public void acceptHereTerms(HereUser hereUser, String termsToken) {
        var termsResponse = hereAccountClient("/terms")
                .body(new AcceptTermsRequest(termsToken, hereUser))
                .post();
        StatusCodeExpects.expectNOCONSTENTStatusCode(termsResponse);
    }

    public void acceptCMConsent(Cookies cookie) {
        Response saveConsentResponse = hereAccountClient("/api/account/save-consent")
                .body(new AcceptConsentRequest())
                .cookies(cookie)
                .post();
        StatusCodeExpects.expectCREATEDStatusCode(saveConsentResponse);
    }


    @Data
    public static class HereUser {

        private String
                firstname,
                email,
                password,
                dob,
                lastname,
                countryCode,
                language,
                phoneNumber,
                realm,
                clientId,
                clientSecret;

        public HereUser(String email, String password, String realm) {
            this.email = email;
            this.password = password;
            firstname = new Faker().name().firstName();
            lastname = new Faker().name().lastName() + "_test";
            countryCode = "USA";
            language = "en";
            dob = "31/01/1980";
            phoneNumber = "+1234567890";
            this.realm = realm;
            clientId = "ha-test-app-1";
            clientSecret = "ha-test-secret-1";
        }

    }

    @Data
    public static class CreateHereUserRequest {

        String firstname,
                email,
                password,
                dob,
                lastname,
                countryCode,
                language,
                phoneNumber,
                realm,
                clientId,
                clientSecret,
                inviteToken;
        boolean terms,
                sendWelcomeEmail;

        public CreateHereUserRequest(HereUser user) {
            this.firstname = user.getFirstname();
            email = user.getEmail();
            password = user.getPassword();
            dob = user.getDob();
            lastname = user.getLastname();
            countryCode = user.getCountryCode();
            language = user.getLanguage();
            phoneNumber = user.getPhoneNumber();
            realm = user.getRealm();
            clientId = user.getClientId();
            clientSecret = user.getClientSecret();
            terms = false;
            inviteToken = "";
            sendWelcomeEmail = false;
        }

    }

    @Data
    public static class GetCurrentTokenRequest {

        String grantType,
                email,
                password,
                countryCode,
                language,
                clientId,
                clientSecret,
                tokenFormat;

        public GetCurrentTokenRequest(HereUser user) {
            grantType = "password";
            email = user.getEmail();
            password = user.getPassword();
            countryCode = user.getCountryCode();
            language = user.getLanguage();
            clientId = user.getClientId();
            clientSecret = user.getClientSecret();
            tokenFormat = "hN";
        }

    }

    @Data
    public static class AcceptTermsRequest {

        private String
                termsReacceptanceToken,
                clientId,
                clientSecret;

        public AcceptTermsRequest(String termsToken, HereUser user) {
            termsReacceptanceToken = termsToken;
            clientId = user.getClientId();
            clientSecret = user.getClientSecret();
        }

    }

    @Data
    public static class AcceptConsentRequest {

        String realm, clientId;
        List<String> scope;

        public AcceptConsentRequest() {
            realm = "HERE";
            clientId = "yEx4GXjPeJtfJKmKOFU5";
            scope = List.of("openid", "email", "profile", "readwrite:ha");
        }

    }

}
