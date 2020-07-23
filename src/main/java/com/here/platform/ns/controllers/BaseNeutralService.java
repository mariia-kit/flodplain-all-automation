package com.here.platform.ns.controllers;

import com.here.platform.ns.dto.Users;
import com.here.platform.ns.utils.NS_Config;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;


import static io.restassured.RestAssured.given;
import static io.restassured.config.HeaderConfig.headerConfig;

public abstract class BaseNeutralService<T> {

    private String authorizationToken = "";

    protected RequestSpecification neutralServerClient(final String targetPath) {
        var baseService = given()
                .config(RestAssuredConfig.config()
                        .headerConfig(headerConfig().overwriteHeadersWithName("Authorization", "Content-Type")))
                .baseUri(NS_Config.URL_NS.toString())
                .basePath(targetPath)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .log().all()
                .filters(new AllureRestAssured());

        if (!authorizationToken.isEmpty()) {
            baseService.header("Authorization", authorizationToken);
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

//    public T withBody(String newBody) {
//        setAuthorizationToken(users.getToken());
//        return (T) this;
//    }

    public void clearBearerToken() {
        this.authorizationToken = "";
    }

}