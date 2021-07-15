package com.here.platform.proxy.admin.provider;

import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.proxy.BaseProxyTests;
import com.here.platform.proxy.conrollers.ServiceProvidersController;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProviderEnum;
import com.here.platform.proxy.helper.ProxyProviderAssertion;
import com.here.platform.proxy.steps.ProxySteps;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("Proxy Admin")
@Tag("proxy_admin")
@DisplayName("[External Proxy] Verify Service Providers Get List By Realm")
public class ProviderGetByRealm extends BaseProxyTests {

    @Test
    @DisplayName("[External Proxy] Get Provider by realm")
    void verifyGetProxyProviderByRealm() {
        String realm = "special-realm";
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        proxyProvider.setProviderRealm(realm);

        ProxySteps.createProxyProvider(proxyProvider);

        var response = new ServiceProvidersController()
                .withAdminToken()
                .getProvidersByRealm(realm);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedProviderInList(proxyProvider);
    }

    @Test
    @DisplayName("[External Proxy] Get Provider by realm No Token")
    void verifyGetProxyProviderByRealmNoToken() {
        String realm = "special-realm";
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        proxyProvider.setProviderRealm(realm);

        ProxySteps.createProxyProvider(proxyProvider);

        var response = new ServiceProvidersController()
                .getProvidersByRealm(realm);
        new ProxyProviderAssertion(response)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("[External Proxy] Get Provider by realm No Realm")
    void verifyGetProxyProviderByRealmNoRealm() {
        String realm = "special-realm-uniq";

        var response = new ServiceProvidersController()
                .withAdminToken()
                .getProvidersByRealm(realm);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK);
    }
}
