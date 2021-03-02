package com.here.platform.ns.controllers.provider;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.controllers.BaseNeutralService;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;


public class ResourceController extends BaseNeutralService<ResourceController> {

    private final String providersBasePath = Conf.ns().getNsUrlProvider() + "providers";

    @Step
    public Response getResource(DataProvider dataProvider, String resourceId) {
        return neutralServerClient(providersBasePath)
                .get("/{providerId}/resources/{resourceId}", dataProvider.getId(), resourceId);

    }

    @Step
    public Response addResource(DataProvider dataProvider, ProviderResource resource) {
        if (!StringUtils.isEmpty(resource.getName())) {
            dataProvider.addResource(resource);
        }
        return neutralServerClient(providersBasePath)
                .body(resource.generateBody())
                .put("/{providerId}/resources/{resourceId}", dataProvider.getId(), resource.getName());
    }

    @Step
    public Response deleteResource(String providerId, String resourceId) {
        return neutralServerClient(providersBasePath)
                .delete("/{providerId}/resources/{resourceId}", providerId, resourceId);
    }

    @Step
    public Response deleteResource(DataProvider dataProvider, ProviderResource resource) {
        return deleteResource(dataProvider.getId(), resource.getName());
    }

    @Step
    public Response getResourceList(String providerId) {
        return neutralServerClient(providersBasePath)
                .get("/{providerId}/resources", providerId);
    }

    @Step
    public Response getResourceList(DataProvider dataProvider) {
        return getResourceList(dataProvider.getId());
    }

}
