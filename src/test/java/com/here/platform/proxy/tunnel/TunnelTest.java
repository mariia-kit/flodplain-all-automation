package com.here.platform.proxy.tunnel;

import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.dto.Users;
import com.here.platform.proxy.BaseProxyTests;
import com.here.platform.proxy.conrollers.TunnelController;
import com.here.platform.proxy.dto.ProxyErrorList;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProvider.CredentialsAuthMethod;
import com.here.platform.proxy.dto.ProxyProviderResource;
import com.here.platform.proxy.dto.ProxyProviderResources;
import com.here.platform.proxy.dto.ProxyProviders;
import com.here.platform.proxy.helper.ProxyProviderAssertion;
import com.here.platform.proxy.helper.RemoveObjCollector;
import com.here.platform.proxy.steps.ProxySteps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("Proxy Tunnel")
@DisplayName("[External Proxy] Verify Proxy Service Tunnel")
public class TunnelTest extends BaseProxyTests {

    @Test
    @DisplayName("[External Proxy] Verify retrieve proxy data Successful")
    void verifyProxyCanBeRetrieved() {
        ProxyProvider proxyProvider = ProxyProviders.REFERENCE_PROXY.getProxyProvider();
        ProxyProviderResource resource = ProxyProviderResources.REFERENCE_RESOURCE.getResource();
        ProxySteps.readProxyProvider(proxyProvider);
        ProxySteps.readProxyProviderResource(resource);
        RemoveObjCollector.addProxyResHrn(resource.getHrn());
        ProxySteps.createListingAndSubscription(resource);

        var tunnel = new TunnelController()
                .withConsumerToken()
                .getData(proxyProvider, resource);
        new ProxyProviderAssertion(tunnel)
                .expectedCode(HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("[External Proxy] Verify retrieve proxy data No Subscription")
    void verifyProxyCanBeRetrievedNoSubs() {
        ProxyProvider proxyProvider = ProxyProviders.REFERENCE_PROXY.getProxyProvider();
        ProxyProviderResource resource = new ProxyProviderResource(
                "Auto-testing-reference-no-subs",
                "/proxy/data/123456nosubs");

        ProxySteps.readProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        var tunnel = new TunnelController()
                .withConsumerToken()
                .getData(proxyProvider, resource);
        new ProxyProviderAssertion(tunnel)
                .expectedError(ProxyErrorList.getNoAccessError(
                        Users.MP_CONSUMER.getUser().getUserId(),
                        resource.getHrn()));
    }

    @Test
    @DisplayName("[External Proxy] Verify retrieve proxy data No Resource No Policy")
    void verifyProxyCanBeRetrievedNoResourceNoPolicy() {
        ProxyProvider proxyProvider = ProxyProviders.REFERENCE_PROXY.getProxyProvider();
        ProxyProviderResource resource = new ProxyProviderResource(
                "Auto-testing-reference-no-policy",
                "/fake/123456nopolicy");

        ProxySteps.readProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);
        ProxySteps.deleteProxyResource(resource.getId());

        var tunnel = new TunnelController()
                .withConsumerToken()
                .getData(proxyProvider, resource);
        new ProxyProviderAssertion(tunnel)
                .expectedError(ProxyErrorList.getResourceNotFoundError(resource.getPath().replaceFirst("/", StringUtils.EMPTY)));
    }

    @DisplayName("[External Proxy] Verify retrieve proxy data Res names intersects with deleted one")
    void verifyProxyCanBeRetrievedResNamesIntersectsDeleted() {
        ProxyProvider proxyProvider = ProxyProviders.REFERENCE_PROXY.getProxyProvider();
        ProxyProviderResource resource = new ProxyProviderResource(
                "Auto-testing-reference-no-res",
                "/proxy/data/123456nores");
        ProxyProviderResource resource2 = new ProxyProviderResource(
                "Auto-testing-reference-no-res",
                "/proxy/data/123456");

        ProxySteps.readProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);
        ProxySteps.createProxyResource(proxyProvider, resource2);
        ProxySteps.deleteProxyResource(resource2.getId());

        var tunnel = new TunnelController()
                .withConsumerToken()
                .getData(proxyProvider, resource);
        new ProxyProviderAssertion(tunnel)
                .expectedError(ProxyErrorList.getNoAccessError(
                        Users.MP_CONSUMER.getUser().getUserId(),
                        resource2.getHrn()));
    }

    @Test
    @DisplayName("[External Proxy] Verify retrieve proxy data No Resource on Data Provider")
    void verifyProxyCanBeRetrievedNoResourceOnDP() {
        ProxyProvider proxyProvider = ProxyProviders.REFERENCE_PROXY.getProxyProvider();
        ProxyProviderResource resource = ProxyProviderResources.generate();
        resource.setPath("/nosuch/url");

        ProxySteps.readProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);
        RemoveObjCollector.addProxyResHrn(resource.getHrn());

        ProxySteps.createListingAndSubscription(resource);

        var tunnel = new TunnelController()
                .withConsumerToken()
                .getData(proxyProvider, resource);
        new ProxyProviderAssertion(tunnel)
                .expectedTunnelError(ProxyErrorList.getResourceNotAccessibleError(resource.getPath()));
    }

    @Test
    @DisplayName("[External Proxy] Verify retrieve proxy data No Token")
    void verifyProxyCanBeRetrievedNoToken() {
        ProxyProvider proxyProvider = ProxyProviders.REFERENCE_PROXY.getProxyProvider();
        ProxyProviderResource resource = new ProxyProviderResource(
                "Auto-testing-reference-no-token",
                "/proxy/data/123456notoken");

        ProxySteps.readProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);

        var tunnel = new TunnelController()
                .getData(proxyProvider, resource);
        new ProxyProviderAssertion(tunnel)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }


    @Test
    @DisplayName("[External Proxy] Verify retrieve new proxy data Successful")
    void verifyNewProxyCanBeRetrieved() {
        ProxyProvider proxyProvider = ProxyProviders.REFERENCE_PROXY.getProxyProvider();
        ProxyProviderResource resource = ProxyProviderResources.generate();

        ProxySteps.readProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);
        RemoveObjCollector.addProxyResHrn(resource.getHrn());

        ProxySteps.createListingAndSubscription(resource);

        var tunnel = new TunnelController()
                .withConsumerToken()
                .getData(proxyProvider, resource);
        new ProxyProviderAssertion(tunnel)
                .expectedCode(HttpStatus.SC_OK)
                .verifyResponseTime(10000L);
        new TunnelController()
                .withConsumerToken()
                .getData(proxyProvider.getDomain(), resource.getPath() + "?query=none");
        var tunnel2 = new TunnelController()
                .withConsumerToken()
                .getData(proxyProvider, resource);
        new ProxyProviderAssertion(tunnel2)
                .expectedCode(HttpStatus.SC_OK)
                .verifyResponseTime(500L);
    }

    @Test
    @DisplayName("[External Proxy] Verify retrieve new proxy data Auth not implemented")
    void verifyNewProxyCanBeRetrievedNoAuth() {
        ProxyProvider proxyProvider = ProxyProviders.generate()
                .withAuthMethod(CredentialsAuthMethod.BASIC_AUTH, "root", "qwerty");
        ProxyProviderResource resource = ProxyProviderResources.generate();

        ProxySteps.createProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);
        RemoveObjCollector.addProxyResHrn(resource.getHrn());

        ProxySteps.createListingAndSubscription(resource);

        var tunnel = new TunnelController()
                .withConsumerToken()
                .getData(proxyProvider, resource);
        new ProxyProviderAssertion(tunnel)
                .expectedCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @Disabled("Reproduced on mp only")
    @DisplayName("[External Proxy] Verify retrieve proxy data Res names intersects")
    void verifyProxyCanBeRetrievedResNamesIntersects() {
        ProxyProvider proxyProvider = ProxyProviders.REFERENCE_PROXY.getProxyProvider();
        ProxyProviderResource resource = ProxyProviderResources.generate();
        ProxyProviderResource resource2 = ProxyProviderResources.generate();
        resource2.setPath(resource.getPath() + "/more");

        ProxySteps.readProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);
        ProxySteps.createProxyResource(proxyProvider, resource2);

        ProxySteps.createListingAndSubscription(resource);

        new TunnelController()
                .withConsumerToken()
                .getData(proxyProvider, resource2);
        var tunnel = new TunnelController()
                .withConsumerToken()
                .getData(proxyProvider, resource);
        new ProxyProviderAssertion(tunnel)
                .expectedError(ProxyErrorList.getNoAccessError(
                        Users.MP_CONSUMER.getUser().getUserId(),
                        resource2.getHrn()));
    }

    @Test
    @DisplayName("[External Proxy] Verify retrieve new proxy data with query")
    void verifyNewProxyCanBeRetrievedWithQuery() {
        ProxyProvider proxyProvider = ProxyProviders.REFERENCE_PROXY.getProxyProvider();
        ProxyProviderResource resource = ProxyProviderResources.generate();

        ProxySteps.readProxyProvider(proxyProvider);
        ProxySteps.createProxyResource(proxyProvider, resource);
        RemoveObjCollector.addProxyResHrn(resource.getHrn());

        ProxySteps.createListingAndSubscription(resource);

        var tunnel = new TunnelController()
                .withConsumerToken()
                .getData(proxyProvider.getDomain(), resource.getPath() + "?query=someData");
        new ProxyProviderAssertion(tunnel)
                .expectedCode(HttpStatus.SC_OK);
    }
}
