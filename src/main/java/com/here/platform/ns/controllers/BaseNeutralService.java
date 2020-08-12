package com.here.platform.ns.controllers;

import static io.restassured.RestAssured.given;
import static io.restassured.config.HeaderConfig.headerConfig;

import com.here.platform.ns.dto.Users;
import com.here.platform.ns.utils.NS_Config;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;


public abstract class BaseNeutralService<T> {

    private String authorizationToken = "";
    private String xcorrId = null;

    protected RequestSpecification neutralServerClient(final String targetPath) {
        var baseService = given()
                .config(RestAssuredConfig.config()
                        .headerConfig(headerConfig().overwriteHeadersWithName("Authorization", "Content-Type")))
                .baseUri(NS_Config.URL_NS.toString())
                .basePath(targetPath)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filters(new AllureRestAssured());

        if (!authorizationToken.isEmpty()) {
            baseService.header("Authorization", authorizationToken);
        }

        if (xcorrId != null) {
            baseService.header("X-Correlation-ID", xcorrId);
        }

        return baseService;
    }

    private void setAuthorizationToken(String tokenValue) {
        //TODO: remove after token refactoring
        this.authorizationToken = String.format("Bearer %s", tokenValue.replace("Bearer ", StringUtils.EMPTY));
    }

    public T withToken(String token) {
        this.authorizationToken = token;
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
