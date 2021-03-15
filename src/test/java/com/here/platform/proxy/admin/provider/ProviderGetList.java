package com.here.platform.proxy.admin.provider;

import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.proxy.BaseProxyTests;
import com.here.platform.proxy.conrollers.ServiceProvidersController;
import com.here.platform.proxy.helper.ProxyProviderAssertion;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("Proxy Admin")
@Tag("proxy_admin")
@DisplayName("[External Proxy] Verify Service Providers Get List")
public class ProviderGetList extends BaseProxyTests {

    @Test
    @DisplayName("[External Proxy] Retrieve all service providers")
    void verifyGetAllProxyProvider() {
        var response = new ServiceProvidersController()
                .withAdminToken()
                .getAllProviders();
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("[External Proxy] Retrieve all service providers No Token")
    void verifyGetAllProxyProviderNoToken() {
        var response = new ServiceProvidersController()
                .getAllProviders();
        new ProxyProviderAssertion(response)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }
}
