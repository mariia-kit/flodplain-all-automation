package com.here.platform.proxy.admin;

import com.here.platform.mp.steps.api.MarketplaceSteps;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.proxy.BaseProxyTests;
import com.here.platform.proxy.conrollers.ServiceProvidersController;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProvider.CredentialsAuthMethod;
import com.here.platform.proxy.dto.ProxyProviderResource;
import com.here.platform.proxy.helper.ProxyProviderAssertion;
import com.here.platform.proxy.steps.ProxySteps;
import io.qameta.allure.Issue;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("Proxy Admin")
@DisplayName("Verify Service Providers Management")
public class ServiceProvidersTest extends BaseProxyTests {

    @Test
    @DisplayName("[External Proxy] Verify retrieve all service providers")
    void verifyGetAllProxyProviderById() {
        var response = new ServiceProvidersController()
                .withAdminToken()
                .getAllProviders();
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK);
    }

    @Test
    @Issue("NS-3576")
    @DisplayName("[External Proxy] Create new External Proxy provider mock and validate provider resource")
    void verifyGetProxyProviderById() {
        ProxyProvider proxyProvider = new ProxyProvider(
                "Auto-testing-1",
                "olp-here-realm-1",
                "someService1.test.mock",
                CredentialsAuthMethod.NONE);
        ProxyProviderResource resource = new ProxyProviderResource("Auto-testing","/forecasts/v1");

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
    @Issue("NS-3580")
    @DisplayName("[External Proxy] Success flow of deleting provider resource")
    void verifyDeleteProxyProviderById() {
        ProxyProvider proxyProvider = new ProxyProvider(
                "Auto-testing-2",
                "olp-here-realm-2",
                "someService2.test.mock",
                CredentialsAuthMethod.NONE);

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
    @Issue("NS-3579")
    @DisplayName("[External Proxy] Provider cannot be deleted if there are attached resources")
    void verifyDeleteProxyProviderWithResource() {
        ProxyProvider proxyProvider = new ProxyProvider(
                "Auto-testing-3",
                "olp-here-realm-3",
                "someService3.test.mock",
                CredentialsAuthMethod.NONE);

        ProxyProviderResource resource = new ProxyProviderResource(
                "Manual-testing-auto",
                "/forecasts/v2");
        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        var delete = new ServiceProvidersController()
                .withAdminToken()
                .deleteProviderById(proxyProvider.getId());
        new ProxyProviderAssertion(delete)
                .expectedCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    @DisplayName("[External Proxy] Verify delete proxy resource by Id")
    void verifyDeleteProxyResourceById() {
        ProxyProvider proxyProvider = new ProxyProvider(
                "Auto-testing-4",
                "olp-here-realm-4",
                "someService4.test.mock",
                CredentialsAuthMethod.NONE);
        ProxyProviderResource resource = new ProxyProviderResource(
                "Manual-testing-auto",
                "/forecasts/v2");
        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        var delete = new ServiceProvidersController()
                .withAdminToken()
                .deleteResourceFromProvider(resource.getId());
        new ProxyProviderAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("[External Proxy] Verify retrieve proxy service resource by Id")
    void verifyGetProxyResourceById() {
        ProxyProvider proxyProvider = new ProxyProvider(
                "Auto-testing-5",
                "olp-here-realm-5",
                "someService5.test.mock",
                CredentialsAuthMethod.NONE);
        ProxyProviderResource resource = new ProxyProviderResource(
                "Manual-testing-auto",
                "/forecasts/v2");
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
    @Issue("NS-3577")
    @DisplayName("[External Proxy] Update external resource and validate provider resource")
    void verifyUpdateProxyResource() {
        ProxyProvider proxyProvider = new ProxyProvider(
                "Auto-testing-6",
                "olp-here-realm-6",
                "someService6.test.mock",
                CredentialsAuthMethod.NONE);
        ProxyProviderResource resource = new ProxyProviderResource(
                "Manual-testing-auto",
                "/forecasts/v2");


        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        ProxyProviderResource newResource = new ProxyProviderResource(
                "Manual-testing-auto-update",
                "/forecasts/v2");
        newResource.setHrn(resource.getHrn());

        var update = new ServiceProvidersController()
                .withAdminToken()
                .updateResourceById(resource.getId(), newResource);
        new ProxyProviderAssertion(update)
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        var get = new ServiceProvidersController()
                .withAdminToken()
                .getResourceByHRN(resource.getHrn());
        new ProxyProviderAssertion(get)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(proxyProvider)
                .expectedResourceInProvider(newResource);
    }

    @Test
    @Issue("NS-3578")
    @Disabled("Cant delete resource after subscription cancel")
    @DisplayName("[External Proxy] Deactivate created subscription and delete provider resource")
    void verifyNewProxyAfterSubscriptionCancel() {
        ProxyProvider proxyProvider = new ProxyProvider(
                "Manual-testing-9",
                "olp-here-mrkt-prov-9",
                "dataservice9.test.mock",
                CredentialsAuthMethod.NONE);
        ProxyProviderResource resource = new ProxyProviderResource(
                "Manual-testing-auto",
                "/forecasts/v2");

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        MarketplaceSteps marketplaceSteps = new MarketplaceSteps();
        String listingHrn = marketplaceSteps.createNewProxyListing(resource.getHrn());
        String subsId = marketplaceSteps.subscribeListing(listingHrn);
        marketplaceSteps.beginCancellation(subsId);
        marketplaceSteps.deleteListing(listingHrn);

        var delete = new ServiceProvidersController()
                .withAdminToken()
                .deleteResourceFromProvider(resource.getId());
        new ProxyProviderAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);
    }

}
