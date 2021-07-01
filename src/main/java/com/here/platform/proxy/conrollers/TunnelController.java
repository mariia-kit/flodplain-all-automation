package com.here.platform.proxy.conrollers;

import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProviderResource;
import io.qameta.allure.Step;
import io.restassured.response.Response;


public class TunnelController extends BaseProxyService<TunnelController> {

    private final String basePath = "/tunnel";

    @Step("Get proxy: {identifier} {path}")
    public Response getData(String identifier, String path) {
        return consentServiceClient(basePath)
                .get(identifier + "/" + path);
    }

    @Step("Get proxy: {provider.identifier} {resource.path}")
    public Response getData(ProxyProvider provider, ProxyProviderResource resource) {
        return consentServiceClient(basePath)
                .get(provider.getIdentifier() + "/" + resource.getPath());
    }
}
