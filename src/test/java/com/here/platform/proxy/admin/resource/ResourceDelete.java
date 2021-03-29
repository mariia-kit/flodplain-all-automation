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
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("Proxy Admin")
@Tag("proxy_admin")
@DisplayName("[External Proxy] Verify Service Resource Delete")
public class ResourceDelete extends BaseProxyTests {

    @Test
    @DisplayName("[External Proxy] Verify delete proxy resource by Id")
    void verifyDeleteProxyResourceById() {
        ProxyProvider proxyProvider = ProxyProviders.generate();
        ProxyProviderResource resource = ProxyProviderResources.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        var delete = new ServiceProvidersController()
                .withAdminToken()
                .deleteResourceFromProvider(resource.getId());
        new ProxyProviderAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("[External Proxy] Verify delete proxy resource by Id No Token")
    void verifyDeleteProxyResourceByIdNoToken() {
        ProxyProvider proxyProvider = ProxyProviders.generate();
        ProxyProviderResource resource = ProxyProviderResources.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        var delete = new ServiceProvidersController()
                .deleteResourceFromProvider(resource.getId());
        new ProxyProviderAssertion(delete)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("[External Proxy] Verify delete proxy resource by Id Not Exist")
    void verifyDeleteProxyResourceByIdNotExist() {
        var delete = new ServiceProvidersController()
                .withAdminToken()
                .deleteResourceFromProvider(-1L);
        new ProxyProviderAssertion(delete)
                .expectedCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("[External Proxy] Verify delete proxy resource by Id Already Deleted")
    void verifyDeleteProxyResourceByIdAlreadyDeleted() {
        ProxyProvider proxyProvider = ProxyProviders.generate();
        ProxyProviderResource resource = ProxyProviderResources.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        var delete = new ServiceProvidersController()
                .withAdminToken()
                .deleteResourceFromProvider(resource.getId());
        new ProxyProviderAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        var delete2 = new ServiceProvidersController()
                .withAdminToken()
                .deleteResourceFromProvider(resource.getId());
        new ProxyProviderAssertion(delete2)
                .expectedCode(HttpStatus.SC_NOT_FOUND);
    }
}
