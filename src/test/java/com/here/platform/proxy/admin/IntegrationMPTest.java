package com.here.platform.proxy.admin;

import com.here.platform.mp.controllers.MarketplaceTunnelController;
import com.here.platform.proxy.BaseProxyTests;
import com.here.platform.proxy.conrollers.ServiceProvidersController;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProviderResource;
import com.here.platform.proxy.dto.ProxyProviderResourceEnum;
import com.here.platform.proxy.dto.ProxyProviderEnum;
import com.here.platform.proxy.helper.ProxyProviderAssertion;
import com.here.platform.proxy.steps.ProxySteps;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("Proxy Admin")
@Tag("proxy_admin")
@DisplayName("[External Proxy] Verify Service Resource Management with Subscriptions")
public class IntegrationMPTest extends BaseProxyTests {

    @Test
    @DisplayName("[External Proxy] Delete resource if Subs removed")
    void verifyDeleteProxyAfterSubscriptionCancel() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        ProxySteps.createAndRemoveListingAndSubscription(resource);

        var delete = new ServiceProvidersController()
                .withAdminToken()
                .deleteResourceFromProvider(resource.getId());
        new ProxyProviderAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("[External Proxy] Delete resource if Subs exist")
    void verifyDeleteProxyAfterSubscriptionExist() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        ProxySteps.createListingAndSubscription(resource);

        var delete = new ServiceProvidersController()
                .withAdminToken()
                .deleteResourceFromProvider(resource.getId());
        new ProxyProviderAssertion(delete)
                .expectedCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("[External Proxy] Update Service resource if subs exist")
    void verifyUpdateProxyResourceSubs() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        ProxyProviderResource newResource = ProxyProviderResourceEnum.generate();
        newResource.setHrn(resource.getHrn());
        //path is not updated!!
        newResource.setPath(resource.getPath());

        ProxySteps.createListingAndSubscription(resource);

        var update = new ServiceProvidersController()
                .withAdminToken()
                .updateResourceById(resource.getId(), newResource);
        new ProxyProviderAssertion(update)
                .expectedCode(HttpStatus.SC_OK);
    }

    @Test
    @Disabled
    @DisplayName("[External Proxy] Update Service resource if subs removed")
    void verifyUpdateProxyResourceSubsRemoved() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        ProxyProviderResource newResource = ProxyProviderResourceEnum.generate();
        newResource.setHrn(resource.getHrn());
        //path is not updated!!
        newResource.setPath(resource.getPath());

        ProxySteps.createAndRemoveListingAndSubscription(resource);

        var update = new ServiceProvidersController()
                .withAdminToken()
                .updateResourceById(resource.getId(), newResource);
        new ProxyProviderAssertion(update)
                .expectedCode(HttpStatus.SC_OK)
                .expectedResourceInProvider(resource);
    }

    @Test
    @Disabled
    @DisplayName("[External Proxy] Marketplace can get list of Providers and Resources")
    void verifyMPCanGetProvidersList() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);
        var verify = new MarketplaceTunnelController()
                .withProviderToken()
                .getProxyServiceProviders();
        new ProxyProviderAssertion(verify)
                .expectedProviderInList(proxyProvider, resource);
    }
}
