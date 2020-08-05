package com.here.platform.hereAccount.controllers;


import com.github.javafaker.Faker;
import com.here.platform.common.JConvert;
import com.here.platform.ns.utils.NS_Config;
import io.restassured.response.Response;
import lombok.Data;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;


public class HereUserManagerController extends BaseHereAccountController {

    public void createHereUser(HereUser hereUser) {
        Assertions.assertEquals("here", hereUser.getRealm(), "Not all realms allow to create users without invite. Please, use 'here'");
        createHereAccount(hereUser);
        String reAcceptanceToken = getHereCurrentToken(hereUser);
        acceptHereTerms(hereUser, reAcceptanceToken);
    }

    public void deleteHereUser(HereUser hereUser) {
        String token = getHereCurrentToken(hereUser);
        deleteHereAccount("Bearer " + token);
    }

    public String createHereAccount(HereUser hereUser) {
        Response create = hereAccountClient("/user")
                .body(new JConvert(new CreateHereUserRequest(hereUser)).toJson())
                .post()
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().response();

        String userId = create.body().jsonPath().get("userId");
        return userId;
    }

    public void deleteHereAccount(String bearerToken) {
        hereAccountClient("/user/me")
                .header("Authorization", bearerToken)
                .delete()
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    public String getHereCurrentToken(HereUser hereUser) {
        Response tokenResponse = hereAccountClient(NS_Config.GET_TOKEN_PATH.toString())
                .header("x-ha-realm", hereUser.getRealm())
                .when().body(new JConvert(new GetCurrentTokenRequest(hereUser)).toJson())
                .post();
        if (tokenResponse.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED) {
            return tokenResponse.jsonPath().get("termsReacceptanceToken");
        } else if (tokenResponse.getStatusCode() == HttpStatus.SC_OK){
            return tokenResponse.jsonPath().get("accessToken");
        } else {
            Assertions.fail("Error receiving token or acceptance token! " + tokenResponse.getBody().prettyPrint());
            return null;
        }
    }

    public void acceptHereTerms(HereUser hereUser, String termsToken) {
        hereAccountClient("/terms")
                .body(new JConvert(new AcceptTermsRequest(termsToken, hereUser)).toJson())
                .post()
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
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
            lastname = new Faker().name().lastName();
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
        String termsReacceptanceToken,
                clientId,
                clientSecret;

        public AcceptTermsRequest(String termsToken, HereUser user) {
            termsReacceptanceToken = termsToken;
            clientId = user.getClientId();
            clientSecret = user.getClientSecret();
        }
    }

}
