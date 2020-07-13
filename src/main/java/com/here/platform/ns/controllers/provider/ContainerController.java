package com.here.platform.ns.controllers.provider;

import com.here.platform.ns.controllers.BaseNeutralService;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.utils.NS_Config;
import io.qameta.allure.Step;
import io.restassured.response.Response;

public class ContainerController extends BaseNeutralService<ContainerController> {

    private final String containersBasePath = NS_Config.SERVICE_PROVIDER + "providers";

    @Step
    public Response getContainer(Container container) {
        return neutralServerClient(containersBasePath)
                .get("/{providerId}/containers_info/{containerId}", container.getDataProviderName(), container.getId());

    }

    @Step
    public Response addContainer(Container container) {
        return neutralServerClient(containersBasePath)
                .body(container.generateBody())
                .put("/{providerId}/containers_info/{containerId}", container.getDataProviderName(), container.getId());
    }

    @Step
    public Response deleteContainer(Container container) {
        return neutralServerClient(containersBasePath)
                .delete("/{providerId}/containers_info/{containerId}", container.getDataProviderName(), container.getId());
    }

    @Step
    public Response getContainersList(String providerId) {
        return neutralServerClient(containersBasePath)
                .get("/{providerId}/containers_info", providerId);
    }

}
