package com.here.platform.proxy.tunnel;

import com.here.platform.mp.steps.api.MarketplaceSteps;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.proxy.BaseProxyTests;
import com.here.platform.proxy.conrollers.ServiceProvidersController;
import com.here.platform.proxy.conrollers.TunnelController;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProvider.CredentialsAuthMethod;
import com.here.platform.proxy.dto.ProxyProviderResource;
import com.here.platform.proxy.dto.ProxyProviderResources;
import com.here.platform.proxy.dto.ProxyProviders;
import com.here.platform.proxy.helper.ProxyProviderAssertion;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("Tunnel")
@DisplayName("Verify Proxy service e2e")
public class TunnelTest extends BaseProxyTests {

    @Test
    @DisplayName("Verify retrieve proxy data Successful")
    void verifyProxyCanBeRetrieved() {
        ProxyProvider proxyProvider = ProxyProviders.REFERENCE_PROXY.getProxyProvider();
        ProxyProviderResource resource = ProxyProviderResources.REFERENCE_RESOURCE.getResource();


        String listingHrn = new MarketplaceSteps().createNewProxyListing(resource.getHrn());
        String subsId = new MarketplaceSteps().subscribeListing(listingHrn);

        var tunnel = new TunnelController()
                .withConsumerToken()
                .getData("reference-data-provider.ost.solo-experiments.com", "proxy/data");
        new NeutralServerResponseAssertion(tunnel)
                .expectedCode(HttpStatus.SC_OK);
    }


    @Test
    @Disabled("Cant delete resource after subscription cancel")
    @DisplayName("Verify retrieve new proxy data Successful")
    void verifyNewProxyCanBeRetrieved() {
        ProxyProvider proxyProvider = new ProxyProvider(
                "Auto-testing-1",
                "olp-sit-mrkt-p2",
                "reference-data-provider.ost.solo-experiments.com",
                CredentialsAuthMethod.NONE);
        proxyProvider.withAuthMethod(CredentialsAuthMethod.API_KEY_IN_QUERY,
                "Authorization", "1f8647f3-5f86-4b5e-8687-982fd620ef78");
        ProxyProviderResource resource = new ProxyProviderResource(
                "Auto-testing-reference",
                "/proxy/data");

        var response = new ServiceProvidersController()
                .withAppToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK);
        String id = String.valueOf(response.getBody().jsonPath().getLong("id"));
        var responseRes = new ServiceProvidersController()
                .withAppToken()
                .addResourceListToProvider(id, resource);
        new ProxyProviderAssertion(responseRes)
                .expectedCode(HttpStatus.SC_OK);
        String resId = String.valueOf(responseRes.getBody().jsonPath().getLong("resources[0].id"));
        String resHrn = responseRes.getBody().jsonPath().getString("resources[0].hrn");

        String listingHrn = new MarketplaceSteps().createNewProxyListing(resHrn);
        String subsId = new MarketplaceSteps().subscribeListing(listingHrn);

        var tunnel = new TunnelController()
                .withConsumerToken()
                .getData("reference-data-provider.ost.solo-experiments.com", "proxy/data");
        new NeutralServerResponseAssertion(tunnel)
                .expectedCode(HttpStatus.SC_OK);
    }
}
