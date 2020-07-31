package com.here.platform.cm.controllers;

import static io.restassured.RestAssured.given;
import static io.restassured.config.HeaderConfig.headerConfig;

import com.here.platform.aaa.BearerAuthorization;
import com.here.platform.cm.enums.ConsentManagementServiceUrl;
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
        this.authorizationToken = String.format("Bearer %s", tokenValue);
    }

    public T withCMToken() {
        setAuthorizationToken(BearerAuthorization.init().getCmUserToken());
        return (T) this;
    }

    public void clearBearerToken() {
        this.authorizationToken = "";
    }

}
