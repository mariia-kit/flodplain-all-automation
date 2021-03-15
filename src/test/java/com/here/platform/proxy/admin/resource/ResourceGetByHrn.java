package com.here.platform.proxy.admin.resource;

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
@DisplayName("[External Proxy] Verify Service Resource Get by Hrn")
public class ResourceGetByHrn extends BaseProxyTests {

    @Test
    @DisplayName("[External Proxy] Verify retrieve proxy service resource by Hrn")
    void verifyGetProxyResourceByHrn() {
        ProxyProvider proxyProvider = ProxyProviders.generate();
        ProxyProviderResource resource = ProxyProviderResources.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        var get = new ServiceProvidersController()
                .withAdminToken()
                .getResourceByHRN(resource.getHrn());
        new ProxyProviderAssertion(get)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(proxyProvider)
                .expectedResourceInProvider(resource);
    }

    @Test
    @DisplayName("[External Proxy] Verify retrieve proxy service resource by Id No Token")
    void verifyGetProxyResourceByHrnNoToken() {
        ProxyProvider proxyProvider = ProxyProviders.generate();
        ProxyProviderResource resource = ProxyProviderResources.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        var get = new ServiceProvidersController()
                .getResourceByHRN(resource.getHrn());
        new ProxyProviderAssertion(get)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("[External Proxy] Verify retrieve proxy service resource by Hrn Not Exist")
    void verifyGetProxyResourceByHrnNotExist() {
        var get = new ServiceProvidersController()
                .withAdminToken()
                .getResourceByHRN("hrn:here:no-such-hrn");
        new ProxyProviderAssertion(get)
                .expectedCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("[External Proxy] Verify retrieve proxy service resource by Hrn Deleted")
    void verifyGetProxyResourceByHrnDeleted() {
        ProxyProvider proxyProvider = ProxyProviders.generate();
        ProxyProviderResource resource = ProxyProviderResources.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);
        new ServiceProvidersController()
                .withAdminToken()
                .deleteResourceFromProvider(resource.getId());

        var get = new ServiceProvidersController()
                .withAdminToken()
                .getResourceByHRN(resource.getHrn());
        new ProxyProviderAssertion(get)
                .expectedCode(HttpStatus.SC_NOT_FOUND);
    }

}
