package com.here.platform.proxy.admin.provider;

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
@DisplayName("[External Proxy] Verify Service Providers Delete")
public class ProviderDelete extends BaseProxyTests {

    @Test
    @DisplayName("[External Proxy] Deleting provider by Id")
    void verifyDeleteProxyProviderById() {
        ProxyProvider proxyProvider = ProxyProviders.generate();

        ProxySteps.createProxyProvider(proxyProvider);

        var delete = new ServiceProvidersController()
                .withAdminToken()
                .deleteProviderById(proxyProvider.getId());
        new ProxyProviderAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);
        var verifyAbsence = new ServiceProvidersController()
                .withAdminToken()
                .getProviderById(proxyProvider.getId());
        new ProxyProviderAssertion(verifyAbsence)
                .expectedCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("[External Proxy] Deleting provider by Id no Token")
    void verifyDeleteProxyProviderByIdNoToken() {
        ProxyProvider proxyProvider = ProxyProviders.generate();

        ProxySteps.createProxyProvider(proxyProvider);

        var delete = new ServiceProvidersController()
                .deleteProviderById(proxyProvider.getId());
        new ProxyProviderAssertion(delete)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("[External Proxy] Deleting provider by Id Already Deleted")
    void verifyDeleteProxyProviderByIdAlreadyDeleted() {
        ProxyProvider proxyProvider = ProxyProviders.generate();

        ProxySteps.createProxyProvider(proxyProvider);

        var delete = new ServiceProvidersController()
                .withAdminToken()
                .deleteProviderById(proxyProvider.getId());
        new ProxyProviderAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);
        var delete2 = new ServiceProvidersController()
                .withAdminToken()
                .deleteProviderById(proxyProvider.getId());
        new ProxyProviderAssertion(delete2)
                .expectedCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("[External Proxy] Provider cannot be deleted if there are attached resources")
    void verifyDeleteProxyProviderWithResource() {
        ProxyProvider proxyProvider = ProxyProviders.generate();
        ProxyProviderResource resource = ProxyProviderResources.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        var delete = new ServiceProvidersController()
                .withAdminToken()
                .deleteProviderById(proxyProvider.getId());
        new ProxyProviderAssertion(delete)
                .expectedCode(HttpStatus.SC_FORBIDDEN)
                .expectedError(ProxyErrorList.getDeleteConflictError(proxyProvider.getId()));
    }

    @Test
    @DisplayName("[External Proxy] Delete provider by id with removed resources")
    void verifyDeleteProxyProviderByIdWithHistory() {
        ProxyProvider proxyProvider = ProxyProviders.generate();
        ProxyProviderResource resource = ProxyProviderResources.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);
        new ServiceProvidersController()
                .withAdminToken()
                .deleteResourceFromProvider(resource.getId());

        var response = new ServiceProvidersController()
                .withAdminToken()
                .getProviderById(proxyProvider.getId());
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK);
    }
}
