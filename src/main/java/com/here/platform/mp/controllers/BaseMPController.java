package com.here.platform.mp.controllers;

import static com.here.platform.common.strings.SBB.sbb;
import static io.restassured.RestAssured.given;
import static io.restassured.config.HeaderConfig.headerConfig;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Users;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;


public abstract class BaseMPController<T> {

    private String authorizationToken = "";

    protected RequestSpecification mpClient(final String targetPath) {
        var baseService = given()
                .config(RestAssuredConfig.config()
                        .headerConfig(headerConfig().overwriteHeadersWithName("Authorization", "Content-Type")))
                .baseUri(Conf.mp().getMarketplaceUrl())
                .basePath(targetPath)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filters(new AllureRestAssured());

        if (!authorizationToken.isEmpty()) {
            baseService.header("Authorization", authorizationToken);
        }

        return baseService;
    }

    public T withBearerToken(String token) {
        this.authorizationToken = sbb("Bearer").w().append(token).bld();
        return (T) this;
    }

    public T withConsumerToken() {
        withBearerToken(Users.MP_CONSUMER.getToken());
        return (T) this;
    }

}
