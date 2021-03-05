package com.here.platform.proxy.tunnel;

import com.here.platform.mp.steps.api.MarketplaceSteps;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.proxy.BaseProxyTests;
import com.here.platform.proxy.conrollers.TunnelController;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProviderResource;
import com.here.platform.proxy.dto.ProxyProviderResources;
import com.here.platform.proxy.dto.ProxyProviders;
import com.here.platform.proxy.helper.RemoveObjCollector;
import com.here.platform.proxy.steps.ProxySteps;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("Proxy Tunnel")
@DisplayName("[External Proxy] Verify Proxy service e2e")
public class TunnelTest extends BaseProxyTests {

    @Test
    @Disabled("Cant delete resource after subscription cancel")
    @DisplayName("[External Proxy] Verify retrieve proxy data Successful")
    void verifyProxyCanBeRetrieved() {
        ProxyProvider proxyProvider = ProxyProviders.REFERENCE_PROXY.getProxyProvider();
        ProxyProviderResource resource = ProxyProviderResources.REFERENCE_RESOURCE.getResource();
        ProxySteps.readProxyProvider(proxyProvider);
        ProxySteps.readProxyProviderResource(resource);
        RemoveObjCollector.addProxyResHrn(resource.getHrn());
        String listingHrn = new MarketplaceSteps().createNewProxyListing(resource.getHrn());
        String subsId = new MarketplaceSteps().subscribeListing(listingHrn);

        var tunnel = new TunnelController()
                .withConsumerToken()
                .getData(proxyProvider, resource);
        new NeutralServerResponseAssertion(tunnel)
                .expectedCode(HttpStatus.SC_OK);
    }


    @Test
    @Disabled("Cant delete resource after subscription cancel")
    @DisplayName("[External Proxy] Verify retrieve new proxy data Successful")
    void verifyNewProxyCanBeRetrieved() {
        ProxyProvider proxyProvider = ProxyProviders.REFERENCE_PROXY.getProxyProvider();
        ProxyProviderResource resource = new ProxyProviderResource(
                "Auto-testing-reference-2",
                "/proxy/data?query=none");

        ProxySteps.readProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);
        RemoveObjCollector.addProxyResHrn(resource.getHrn());

        String listingHrn = new MarketplaceSteps().createNewProxyListing(resource.getHrn());
        String subsId = new MarketplaceSteps().subscribeListing(listingHrn);

        var tunnel = new TunnelController()
                .withConsumerToken()
                .getData(proxyProvider, resource);
        new NeutralServerResponseAssertion(tunnel)
                .expectedCode(HttpStatus.SC_OK);
    }
}
