package com.here.platform.proxy.steps;

import com.here.platform.proxy.conrollers.ServiceProvidersController;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProviderResource;
import com.here.platform.proxy.helper.ProxyProviderAssertion;
import io.qameta.allure.Step;
import java.util.Arrays;
import lombok.experimental.UtilityClass;
import org.apache.http.HttpStatus;


@UtilityClass
public class ProxySteps {


    @Step("Create regular proxy provider {proxyProvider.serviceName}")
    public void createProxyProvider(ProxyProvider proxyProvider) {
        var response = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK);
        Long id = response.getBody().jsonPath().getLong("id");
        String scbeId = response.getBody().jsonPath().getString("scbeId");
        proxyProvider.setId(id);
        proxyProvider.setScbeId(scbeId);
    }

    @Step("Create regular proxy provider resource {proxyProvider.serviceName} {proxyProviderResource.title}")
    public void createProxyResource(ProxyProvider proxyProvider, ProxyProviderResource proxyProviderResource) {
        var responseRes = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(String.valueOf(proxyProvider.getId()), proxyProviderResource);
        new ProxyProviderAssertion(responseRes)
                .expectedCode(HttpStatus.SC_OK);
        Long resId = responseRes.getBody().jsonPath().getLong("resources[0].id");
        String resHrn = responseRes.getBody().jsonPath().getString("resources[0].hrn");
        proxyProviderResource.setId(resId);
        proxyProviderResource.setHrn(resHrn);
    }

    @Step("Read proxy provider {proxyProvider.serviceName} data.")
    public void readProxyProvider(ProxyProvider proxyProvider) {
        var response = new ServiceProvidersController()
                .withAdminToken()
                .getAllProviders();
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK);
        var provider = response.getBody().as(ProxyProvider[].class);
        Arrays.stream(provider).filter(pr -> pr.getServiceName().equals(proxyProvider.getServiceName()))
                .findAny().ifPresent(pr -> {
                    proxyProvider.setId(pr.getId());
                    proxyProvider.setScbeId(pr.getScbeId());
        });
    }

    @Step("Read proxy provider resource {proxyProviderResource.title} data.")
    public void readProxyProviderResource(ProxyProviderResource proxyProviderResource) {
        var response = new ServiceProvidersController()
                .withAdminToken()
                .getAllProviders();
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK);
        var provider = response.getBody().as(ProxyProvider[].class);
        Arrays.stream(provider).flatMap(pr -> pr.getResources().stream())
                .filter(res -> res.getTitle().equals(proxyProviderResource.getTitle()))
                .findAny().ifPresent(res -> {
                    proxyProviderResource.setId(res.getId());
                    proxyProviderResource.setHrn(res.getHrn());
        });
    }
}
