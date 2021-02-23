package com.here.platform.proxy.conrollers;

import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProviderResource;
import com.here.platform.proxy.dto.ProxyResourceRequest;
import com.here.platform.proxy.helper.RemoveObjCollector;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.HttpStatus;


public class ServiceProvidersController extends BaseProxyService<ServiceProvidersController> {

    private final String basePath = "/admin/serviceProviders";

    @Step("Get proxy all service providers")
    public Response getAllProviders() {
        return consentServiceClient(basePath)
                .get();
    }

    @Step("Get proxy service provider {serviceProviderId}")
    public Response getProviderById(String serviceProviderId) {
        return consentServiceClient(basePath)
                .get("/{serviceProviderId}", serviceProviderId);
    }

    @Step("Add proxy service provider {proxyProvider.serviceName}")
    public Response addProvider(ProxyProvider proxyProvider) {
        Response response = consentServiceClient(basePath)
                .body(proxyProvider)
                .put();
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            String id = String.valueOf(response.getBody().jsonPath().getLong("id"));
            RemoveObjCollector.addProxyProvider(id);
        }
        return response;
    }

    @Step("Remove proxy service provider with id {serviceProviderId}")
    public Response deleteProviderById(String serviceProviderId) {
        return consentServiceClient(basePath)
                .delete("/{serviceProviderId}", serviceProviderId);
    }

    @Step("Add resources list to proxy provider {serviceProviderId}")
    public Response addResourceListToProvider(String serviceProviderId, List<ProxyProviderResource> resources) {
        ProxyResourceRequest proxyResourceRequest = new ProxyResourceRequest(resources);
        Response response = consentServiceClient(basePath)
                .body(proxyResourceRequest)
                .put("/{serviceProviderId}/resources", serviceProviderId);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            List<ProxyProviderResource> result = response.getBody().jsonPath()
                    .getList("resources", ProxyProviderResource.class);
            result.forEach(res -> RemoveObjCollector.addResourceToProxyProvider(serviceProviderId, String.valueOf(res.getId())));
        }
        return response;
    }

    public Response addResourceListToProvider(String serviceProviderId, ProxyProviderResource resources) {
        List<ProxyProviderResource> resList = new LinkedList<>();
        resList.add(resources);
        return addResourceListToProvider(serviceProviderId, resList);
    }

    @Step("Delete Proxy resource with id {resourceId}")
    public Response deleteResourceFromProvider(String resourceId) {
        Response response = consentServiceClient(basePath)
                .delete("/resources/{resourceId}", resourceId);
        if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            RemoveObjCollector.removeResourceFromProxyProvider(resourceId);
        }
        return response;
    }

}
