package com.here.platform.proxy.admin.provider;

import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.proxy.BaseProxyTests;
import com.here.platform.proxy.conrollers.ServiceProvidersController;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProviderResource;
import com.here.platform.proxy.dto.ProxyProviderResourceEnum;
import com.here.platform.proxy.dto.ProxyProviderEnum;
import com.here.platform.proxy.helper.ProxyProviderAssertion;
import com.here.platform.proxy.steps.ProxySteps;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("Proxy Admin")
@Tag("proxy_admin")
@DisplayName("[External Proxy] Verify Service Providers Get Resource List")
public class ProviderGetResources extends BaseProxyTests {

    @Test
    @DisplayName("[External Proxy] Get resource list")
    void verifyGetResourceList() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();
        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        var get = new ServiceProvidersController()
                .withAdminToken()
                .getResourceOfProvider(proxyProvider.getId());
        new ProxyProviderAssertion(get)
                .expectedCode(HttpStatus.SC_OK)
                .expectedResourceInProvider(resource);
    }

    @Test
    @DisplayName("[External Proxy] Get resource list No token")
    void verifyGetResourceListNoToken() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();
        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        var get = new ServiceProvidersController()
                .getResourceOfProvider(proxyProvider.getId());
        new ProxyProviderAssertion(get)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("[External Proxy] Get resource list No resource")
    void verifyGetResourceListNoResource() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxySteps.createProxyProvider(proxyProvider);

        var get = new ServiceProvidersController()
                .withAdminToken()
                .getResourceOfProvider(proxyProvider.getId());
        new ProxyProviderAssertion(get)
                .expectedCode(HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("[External Proxy] Get resource list No Provider")
    void verifyGetResourceListNoProvider() {
        var get = new ServiceProvidersController()
                .withAdminToken()
                .getResourceOfProvider(-1L);
        new ProxyProviderAssertion(get)
                .expectedCode(HttpStatus.SC_OK);
    }
}
