package com.here.platform.proxy.admin.resource;

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
@DisplayName("[External Proxy] Verify Service Resource Update")
public class ResourceUpdate extends BaseProxyTests {

    @Test
    @DisplayName("[External Proxy] Update Service resource")
    void verifyUpdateProxyResource() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();


        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        ProxyProviderResource newResource = ProxyProviderResourceEnum.generate();
        newResource.setHrn(resource.getHrn());
        //path is not updated!!
        newResource.setPath(resource.getPath());

        var update = new ServiceProvidersController()
                .withAdminToken()
                .updateResourceById(resource.getId(), newResource);
        new ProxyProviderAssertion(update)
                .expectedCode(HttpStatus.SC_OK);

        var get = new ServiceProvidersController()
                .withAdminToken()
                .getResourceByHRN(resource.getHrn());
        new ProxyProviderAssertion(get)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(proxyProvider)
                .expectedResourceInProvider(newResource);
    }

    @Test
    @DisplayName("[External Proxy] Update Service resource No Token")
    void verifyUpdateProxyResourceNoToken() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();


        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        ProxyProviderResource newResource = ProxyProviderResourceEnum.generate();
        newResource.setHrn(resource.getHrn());

        var update = new ServiceProvidersController()
                .updateResourceById(resource.getId(), newResource);
        new ProxyProviderAssertion(update)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }
}
