package com.here.platform.ns.controllers.provider;

import com.here.platform.ns.controllers.BaseNeutralService;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.utils.NS_Config;
import io.qameta.allure.Step;
import io.restassured.response.Response;

public class ProviderController extends BaseNeutralService<ProviderController> {

    private final String containersBasePath = NS_Config.SERVICE_PROVIDER + "providers";

    @Step
    public Response addProvider(DataProvider provider) {
        return neutralServerClient(containersBasePath)
                .body(provider.generateBody())
                .put("/{providerId}", provider.getName());
    }

    @Step
    public Response deleteProvider(DataProvider provider) {
        return deleteProvider(provider.getName());
    }

    @Step
    public Response deleteProvider(String providerId) {
        return neutralServerClient(containersBasePath)
                .delete("/{providerId}", providerId);
    }

    @Step
    public Response getProviderList() {
        return neutralServerClient(containersBasePath)
                .get();
    }
}