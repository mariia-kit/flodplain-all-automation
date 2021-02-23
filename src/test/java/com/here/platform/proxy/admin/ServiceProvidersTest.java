package com.here.platform.proxy.admin;

import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.proxy.BaseProxyTests;
import com.here.platform.proxy.conrollers.ServiceProvidersController;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProvider.CredentialsAuthMethod;
import com.here.platform.proxy.dto.ProxyProviderResource;
import com.here.platform.proxy.helper.ProxyProviderAssertion;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("Tunnel")
@DisplayName("Verify Service Providers Management")
public class ServiceProvidersTest extends BaseProxyTests {

    @Test
    @DisplayName("Verify retrieve all service providers")
    void verifyGetAllProxyProviderById() {
        var response = new ServiceProvidersController()
                .withAppToken()
                .getAllProviders();
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("Verify retrieve proxy service provider by Id")
    void verifyGetProxyProviderById() {
        var response = new ServiceProvidersController()
                .withAppToken()
                .getProviderById("6");
        ProxyProvider expected = new ProxyProvider(
                "Manual-testing",
                "olp-here-mrkt-prov-6",
                "dataservice.test.mock",
                null);
        ProxyProviderResource expectedResource = new ProxyProviderResource(37L,
                "Manual-testing",
                "/forecasts/v1",
                "hrn:here-dev:extsvc::olp-here-mrkt-prov-6:9256ddb5-dataservice_test_mock-forecasts_v1");
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(expected)
                .expectedResourceInProvider(expectedResource);
    }

    @Test
    @DisplayName("Verify delete proxy service provider by Id")
    void verifyDeleteProxyProviderById() {
        ProxyProvider proxyProvider = new ProxyProvider(
                "Manual-testing-2",
                "olp-here-mrkt-prov-6",
                "dataservice2.test.mock",
                CredentialsAuthMethod.NONE);
        var response = new ServiceProvidersController()
                .withAppToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK);
        String id = String.valueOf(response.getBody().jsonPath().getLong("id"));
        var delete = new ServiceProvidersController()
                .withAppToken()
                .deleteProviderById(id);
        new ProxyProviderAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);
        var verifyAbsence = new ServiceProvidersController()
                .withAppToken()
                .getProviderById(id);
        new ProxyProviderAssertion(verifyAbsence)
                .expectedCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Verify delete proxy resource by Id")
    void verifyDeleteProxyResourceById() {
        ProxyProvider proxyProvider = new ProxyProvider(
                "Manual-testing-3",
                "olp-here-mrkt-prov-6",
                "dataservice3.test.mock",
                CredentialsAuthMethod.NONE);
        ProxyProviderResource resource = new ProxyProviderResource(
                "Manual-testing-auto",
                "/forecasts/v2");
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

        var delete = new ServiceProvidersController()
                .withAppToken()
                .deleteResourceFromProvider(resId);
        new ProxyProviderAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);
    }

}
