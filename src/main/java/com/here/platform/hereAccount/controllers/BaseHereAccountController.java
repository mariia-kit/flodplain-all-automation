package com.here.platform.hereAccount.controllers;

import static io.restassured.RestAssured.given;

import com.here.platform.common.config.Conf;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;


public class BaseHereAccountController {

    protected RequestSpecification hereAccountClient(final String targetPath) {
        return given()
                .baseUri(Conf.ns().getAuthUrlBase())
                .basePath(targetPath)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

}
