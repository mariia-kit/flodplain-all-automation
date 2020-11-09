package com.here.platform.ns.controllers.access;

import com.here.platform.common.config.Conf;
import io.qameta.allure.Step;
import io.restassured.response.Response;


public class AccessServiceController extends BaseNeutralServerAccessController<AccessServiceController> {

    private final String serviceBasePath = Conf.ns().getNsUrlAccess();

    @Step
    public Response getHealth() {
        return neutralServerAccessClient(serviceBasePath)
                .get("/health");
    }

    @Step
    public Response getHealthDeep() {
        return neutralServerAccessClient(serviceBasePath)
                .get("/healthDeep");
    }

    @Step
    public Response getVersion() {
        return neutralServerAccessClient(serviceBasePath)
                .get("/version");
    }

}
