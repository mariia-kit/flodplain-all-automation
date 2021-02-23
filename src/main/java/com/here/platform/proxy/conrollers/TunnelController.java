package com.here.platform.proxy.conrollers;

import io.qameta.allure.Step;
import io.restassured.response.Response;


public class TunnelController extends BaseProxyService<TunnelController> {

    private final String basePath = "/tunnel";

    @Step("Get proxy: {domain} {path}")
    public Response getData(String domain, String path) {
        return consentServiceClient(basePath)
                .get("{domain}/" + path, domain);
    }
}
