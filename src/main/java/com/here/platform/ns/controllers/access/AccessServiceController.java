package com.here.platform.ns.controllers.access;

import com.here.platform.ns.controllers.BaseNeutralService;
import com.here.platform.ns.utils.NS_Config;
import io.qameta.allure.Step;
import io.restassured.response.Response;


public class AccessServiceController extends BaseNeutralService<AccessServiceController> {

    private final String serviceBasePath = NS_Config.SERVICE_ACCESS.toString();

    @Step
    public Response getHealth() {
        return neutralServerClient(serviceBasePath)
                .get("/health");
    }

    @Step
    public Response getHealthDeep() {
        return neutralServerClient(serviceBasePath)
                .get("/healthDeep");
    }

    @Step
    public Response getVersion() {
        return neutralServerClient(serviceBasePath)
                .get("/version");
    }

}
