package com.here.platform.proxy.conrollers;

import com.here.platform.proxy.dto.AwsS3Provider;
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
    public Response getProviderById(Long serviceProviderId) {
        return consentServiceClient(basePath)
                .get("/{serviceProviderId}", serviceProviderId);
    }

    @Step("Get proxy service provider resource by hrn {resourceHrn}")
    public Response getResourceByHRN(String resourceHrn) {
        return consentServiceClient(basePath)
                .get("/resources/{resourceHrn}", resourceHrn);
    }

    @Step("Get proxy service provider resources by id {providerId}")
    public Response getResourceOfProvider(Long providerId) {
        return consentServiceClient(basePath)
                .get("/{providerId}/resources", providerId);
    }

    @Step("Add proxy service provider {proxyProvider.serviceName}")
    public Response addProvider(ProxyProvider proxyProvider) {
        Response response = consentServiceClient(basePath)
                .body(proxyProvider)
                .post();
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            Long id = response.getBody().jsonPath().getLong("id");
            RemoveObjCollector.addProxyProvider(id);
        }
        return response;
    }

    @Step("Add AWS S3 service provider {awsS3Provider.serviceName}")
    public Response addAwsS3Provider(AwsS3Provider awsS3Provider) {
        Response response = consentServiceClient(basePath)
                .body(awsS3Provider)
                .post();
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            Long id = response.getBody().jsonPath().getLong("id");
            RemoveObjCollector.addProxyProvider(id);
        }
        return response;
    }

    @Step("Remove proxy service provider with id {serviceProviderId}")
    public Response deleteProviderById(Long serviceProviderId) {
        return consentServiceClient(basePath)
                .delete("/{serviceProviderId}", serviceProviderId);
    }

    @Step("Add resources list to proxy provider {serviceProviderId}")
    public Response addResourceListToProvider(Long serviceProviderId, List<ProxyProviderResource> resources) {
        ProxyResourceRequest proxyResourceRequest = new ProxyResourceRequest(resources);
        Response response = consentServiceClient(basePath)
                .body(proxyResourceRequest)
                .post("/{serviceProviderId}/resources", serviceProviderId);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            List<ProxyProviderResource> result = response.getBody().jsonPath()
                    .getList("resources", ProxyProviderResource.class);
            result.forEach(res -> RemoveObjCollector.addResourceToProxyProvider(String.valueOf(serviceProviderId), res.getId()));
        }
        return response;
    }

    public Response addResourceListToProvider(Long serviceProviderId, ProxyProviderResource resources) {
        List<ProxyProviderResource> resList = new LinkedList<>();
        resList.add(resources);
        return addResourceListToProvider(serviceProviderId, resList);
    }

    @Step("Delete Proxy resource with id {resourceId}")
    public Response deleteResourceFromProvider(Long resourceId) {
        Response response = consentServiceClient(basePath)
                .delete("/resources/{resourceId}", resourceId);
        if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            RemoveObjCollector.removeResourceFromProxyProvider(resourceId);
        }
        return response;
    }

    @Step("Update service provider resource {newResource.title}")
    public Response updateResourceById(Long resourceId, ProxyProviderResource newResource) {
        return consentServiceClient(basePath)
                .body(newResource)
                .put("/resources/{resourceId}", resourceId);
    }

    @Step("Get List of all Providers wit realm {providerRealm}")
    public Response getProvidersByRealm(String providerRealm) {
        return consentServiceClient(basePath)
                .get("/realms/{providerRealm}", providerRealm);
    }
}
