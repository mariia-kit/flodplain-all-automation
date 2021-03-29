package com.here.platform.proxy.admin.resource;

import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.proxy.BaseProxyTests;
import com.here.platform.proxy.conrollers.ServiceProvidersController;
import com.here.platform.proxy.dto.ProxyErrorList;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProviderResource;
import com.here.platform.proxy.dto.ProxyProviderResources;
import com.here.platform.proxy.dto.ProxyProviders;
import com.here.platform.proxy.helper.ProxyProviderAssertion;
import com.here.platform.proxy.steps.ProxySteps;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("Proxy Admin")
@Tag("proxy_admin")
@DisplayName("[External Proxy] Verify Service Resource Add Resource List")
public class ResourceAdd extends BaseProxyTests {

    @Test
    @DisplayName("[External Proxy] Add Resources to Proxy Provider")
    void verifyAddResourcesToProvider() {
        ProxyProvider proxyProvider = ProxyProviders.generate();
        ProxyProviderResource resource = ProxyProviderResources.generate();
        ProxySteps.createProxyProvider(proxyProvider);

        var responseRes = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(proxyProvider.getId(), resource);
        new ProxyProviderAssertion(responseRes)
                .expectedCode(HttpStatus.SC_OK);
        Long resId = responseRes.getBody().jsonPath().getLong("resources[0].id");
        String resHrn = responseRes.getBody().jsonPath().getString("resources[0].hrn");
        resource.setId(resId);
        resource.setHrn(resHrn);

        new ProxyProviderAssertion(responseRes)
                .expectedResourceInProvider(resource);
    }

    @Test
    @DisplayName("[External Proxy] Add Resources to Proxy Provider No Token")
    void verifyAddResourcesToProviderNoToken() {
        ProxyProvider proxyProvider = ProxyProviders.generate();
        ProxyProviderResource resource = ProxyProviderResources.generate();
        ProxySteps.createProxyProvider(proxyProvider);

        var responseRes = new ServiceProvidersController()
                .addResourceListToProvider(proxyProvider.getId(), resource);
        new ProxyProviderAssertion(responseRes)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("[External Proxy] Add Resources to Proxy Provider Multiple")
    void verifyAddResourcesToProviderMultiple() {
        ProxyProvider proxyProvider = ProxyProviders.generate();
        ProxyProviderResource resource = ProxyProviderResources.generate();
        ProxyProviderResource resource2 = ProxyProviderResources.generate();
        ProxySteps.createProxyProvider(proxyProvider);

        var responseRes = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(proxyProvider.getId(), List.of(resource, resource2));
        new ProxyProviderAssertion(responseRes)
                .expectedCode(HttpStatus.SC_OK);
        Long resId = responseRes.getBody().jsonPath().getLong("resources[0].id");
        String resHrn = responseRes.getBody().jsonPath().getString("resources[0].hrn");
        resource.setId(resId);
        resource.setHrn(resHrn);
        Long resId2 = responseRes.getBody().jsonPath().getLong("resources[1].id");
        String resHrn2 = responseRes.getBody().jsonPath().getString("resources[1].hrn");
        resource2.setId(resId2);
        resource2.setHrn(resHrn2);

        new ProxyProviderAssertion(responseRes)
                .expectedCode(HttpStatus.SC_OK)
                .expectedResourceInProvider(resource)
                .expectedResourceInProvider(resource2);
    }

    @Test
    @DisplayName("[External Proxy] Add Resources to Proxy Provider Already Exist")
    void verifyAddResourcesToProviderAlreadyExist() {
        ProxyProvider proxyProvider = ProxyProviders.generate();
        ProxyProviderResource resource = ProxyProviderResources.generate();
        ProxySteps.createProxyProvider(proxyProvider);

        var responseRes = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(proxyProvider.getId(), resource);
        new ProxyProviderAssertion(responseRes)
                .expectedCode(HttpStatus.SC_OK);
        var responseRes2 = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(proxyProvider.getId(), resource);

        new ProxyProviderAssertion(responseRes2)
                .expectedError(ProxyErrorList.getProviderResourceAlreadyExistsError(resource.getTitle(), resource.getPath()));
    }

    @Test
    @DisplayName("[External Proxy] Add Resources to Proxy Provider No Provider")
    void verifyAddResourcesToProviderNoProvider() {
        ProxyProviderResource resource = ProxyProviderResources.generate();

        var responseRes = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(-1L, resource);

        new ProxyProviderAssertion(responseRes)
                .expectedError(ProxyErrorList.getProviderNotFoundError(-1L));
    }

    @Test
    @DisplayName("[External Proxy] Add Resources to Proxy Provider with Query")
    void verifyAddResourcesToProviderWithQuery() {
        ProxyProvider proxyProvider = ProxyProviders.generate();
        ProxyProviderResource resource = ProxyProviderResources.generate();
        resource.setPath(resource.getPath() + "?query=someData");
        ProxySteps.createProxyProvider(proxyProvider);

        var responseRes = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(proxyProvider.getId(), resource);
        new ProxyProviderAssertion(responseRes)
                .expectedCode(HttpStatus.SC_OK);
        Long resId = responseRes.getBody().jsonPath().getLong("resources[0].id");
        String resHrn = responseRes.getBody().jsonPath().getString("resources[0].hrn");
        resource.setId(resId);
        resource.setHrn(resHrn);

        new ProxyProviderAssertion(responseRes)
                .expectedResourceInProvider(resource);
    }
}
