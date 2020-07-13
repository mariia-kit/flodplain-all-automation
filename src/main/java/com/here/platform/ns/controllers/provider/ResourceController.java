package com.here.platform.ns.controllers.provider;

import com.here.platform.ns.controllers.BaseNeutralService;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.utils.NS_Config;
import io.qameta.allure.Step;
import io.restassured.response.Response;

public class ResourceController extends BaseNeutralService<ResourceController> {

    private final String containersBasePath = NS_Config.SERVICE_PROVIDER + "providers";

    @Step
    public Response getResource(DataProvider dataProvider, String resourceId) {
        return neutralServerClient(containersBasePath)
                .get("/{providerId}/resources/{resourceId}", dataProvider.getName(), resourceId);

    }

    @Step
    public Response addResource(DataProvider dataProvider, ProviderResource resource) {
        return neutralServerClient(containersBasePath)
                .body(resource.generateBody())
                .put("/{providerId}/resources/{resourceId}", dataProvider.getName(), resource.getName());
    }

    @Step
    public Response deleteResource(String providerId, String resourceId) {
        return neutralServerClient(containersBasePath)
                .delete("/{providerId}/resources/{resourceId}", providerId, resourceId);
    }

    @Step
    public Response getResourceList(String providerId) {
        return neutralServerClient(containersBasePath)
                .get("/{providerId}/resources", providerId);
    }

}