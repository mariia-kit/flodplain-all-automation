package com.here.platform.hereAccount.controllers;

import static io.restassured.RestAssured.given;

import com.here.platform.ns.utils.NS_Config;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;


public class BaseHereAccountController {

    protected RequestSpecification hereAccountClient(final String targetPath) {
        var baseService = given()
                .log().all()
                .baseUri(NS_Config.URL_AUTH.toString())
                .basePath(targetPath)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
        return baseService;
    }
}
