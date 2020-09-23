package com.here.platform.ns.controllers;

import static io.restassured.RestAssured.given;
import static io.restassured.config.HeaderConfig.headerConfig;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.AllureRestAssuredCustom;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;


public abstract class BaseNeutralService<T> {

    private String authorizationToken = "";
    private String xcorrId = null;

    protected RequestSpecification neutralServerClient(final String targetPath, String basePath) {
        var baseService = given()
                .config(RestAssuredConfig.config()
                        .headerConfig(headerConfig().overwriteHeadersWithName("Authorization", "Content-Type")))
                .baseUri(basePath)
                .basePath(targetPath)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filters(new AllureRestAssuredCustom(targetPath));

        if (!authorizationToken.isEmpty()) {
            baseService.header("Authorization", authorizationToken);
        }

        if (xcorrId != null) {
            baseService.header("X-Correlation-ID", xcorrId);
        }

        return baseService;
    }

    protected RequestSpecification neutralServerClient(final String targetPath) {
        return neutralServerClient(targetPath, Conf.ns().getNsUrlBaseProvider());
    }

    private void setAuthorizationToken(String tokenValue) {
        this.authorizationToken = String.format("Bearer %s", tokenValue);
    }

    public T withToken(String token) {
        this.authorizationToken = token;
        return (T) this;
    }

    public T withBearerToken(String userToken) {
        setAuthorizationToken(userToken);
        return (T) this;
    }

    public T withToken(Users users) {
        setAuthorizationToken(users.getToken());
        return (T) this;
    }

    public T withXCorrelationId(String xcorrId) {
        this.xcorrId = xcorrId;
        return (T) this;
    }

    public void clearBearerToken() {
        this.authorizationToken = "";
    }

}
