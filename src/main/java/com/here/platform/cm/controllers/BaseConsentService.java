package com.here.platform.cm.controllers;

import static com.here.platform.common.strings.SBB.sbb;
import static io.restassured.RestAssured.given;
import static io.restassured.config.HeaderConfig.headerConfig;

import com.here.platform.aaa.BearerAuthorization;
import com.here.platform.cm.enums.ConsentManagementServiceUrl;
import com.here.platform.cm.enums.MPConsumers;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;


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

        if (!authorizationToken.isEmpty()) {
            baseService.header("Authorization", authorizationToken);
        }

        return baseService;
    }

    private void setAuthorizationToken(String tokenValue) {
        this.authorizationToken = sbb("Bearer").w().append(tokenValue).bld();
    }

    public T withCMToken() {
        setAuthorizationToken(BearerAuthorization.init().getCmUserToken());
        return (T) this;
    }

    public T withConsumerToken() {
        setAuthorizationToken(MPConsumers.OLP_CONS_1.getToken());
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
