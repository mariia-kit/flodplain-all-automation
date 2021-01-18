package com.here.platform.cm.controllers;

import static com.here.platform.common.strings.SBB.sbb;
import static io.restassured.RestAssured.given;
import static io.restassured.config.HeaderConfig.headerConfig;

import com.here.platform.aaa.BearerAuthorization;
import com.here.platform.cm.enums.ConsentManagementServiceUrl;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.authentication.AuthController;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.util.StringUtils;


abstract class BaseConsentService<T> {

    private String authorizationToken = "";

    protected RequestSpecification consentServiceClient(final String targetPath) {
        var baseService = given()
                .config(RestAssuredConfig.config()
                        .headerConfig(headerConfig().overwriteHeadersWithName("Authorization", "Content-Type")))
                .baseUri(ConsentManagementServiceUrl.getEnvUrl())
                .basePath(targetPath)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filters(new AllureRestAssured());

        if (!StringUtils.isEmpty(authorizationToken)) {
            baseService.header("Authorization", authorizationToken);
        }

        return baseService;
    }

    private void setAuthorizationToken(String tokenValue) {
        this.authorizationToken = sbb("Bearer").w().append(tokenValue).bld();
    }

    public T withCMToken() {
        setAuthorizationToken(AuthController
                .loadOrGenerate("CmToken_" + System.getProperty("env"),
                        () -> BearerAuthorization.init().getCmUserToken()));
        return (T) this;
    }

    public T withConsumerToken() {
        setAuthorizationToken(Users.MP_CONSUMER.getToken());
        return (T) this;
    }

    public T withConsumerToken(User consumerToken) {
        setAuthorizationToken(consumerToken.getToken());
        return (T) this;
    }

    public T withAuthorizationValue(String authorizationTokenValue) {
        setAuthorizationToken(authorizationTokenValue);
        return (T) this;
    }

    public void clearBearerToken() {
        this.authorizationToken = "";
    }

}
