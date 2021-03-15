package com.here.platform.proxy.admin.provider;

import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.proxy.BaseProxyTests;
import com.here.platform.proxy.conrollers.ServiceProvidersController;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProviderResource;
import com.here.platform.proxy.dto.ProxyProviderResources;
import com.here.platform.proxy.dto.ProxyProviders;
import com.here.platform.proxy.helper.ProxyProviderAssertion;
import com.here.platform.proxy.steps.ProxySteps;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("Proxy Admin")
@Tag("proxy_admin")
@DisplayName("[External Proxy] Verify Service Providers Get By Id")
public class ProviderGetById extends BaseProxyTests {

    @Test
    @DisplayName("[External Proxy] Get Provider by id")
    void verifyGetProxyProviderById() {
        ProxyProvider proxyProvider = ProxyProviders.generate();
        ProxyProviderResource resource = ProxyProviderResources.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        var response = new ServiceProvidersController()
                .withAdminToken()
                .getProviderById(proxyProvider.getId());
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(proxyProvider)
                .expectedResourceInProvider(resource);
    }

    @Test
    @DisplayName("[External Proxy] Get Provider by id no Token")
    void verifyGetProxyProviderByIdNoToken() {
        ProxyProvider proxyProvider = ProxyProviders.generate();
        ProxyProviderResource resource = ProxyProviderResources.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        var response = new ServiceProvidersController()
                .getProviderById(proxyProvider.getId());
        new ProxyProviderAssertion(response)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("[External Proxy] Verify Get Provider by id not Exist")
    void verifyGetProxyProviderByIdNotExist() {
        var response = new ServiceProvidersController()
                .withAdminToken()
                .getProviderById(-123332L);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_NOT_FOUND);
    }
}
