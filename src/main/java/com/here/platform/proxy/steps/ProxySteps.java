package com.here.platform.proxy.steps;

import com.here.platform.common.config.Conf;
import com.here.platform.mp.steps.api.MarketplaceSteps;
import com.here.platform.proxy.conrollers.ServiceProvidersController;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProviderResource;
import com.here.platform.proxy.helper.ProxyAAController;
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

    @Step("Delete proxy provider {providerId}")
    public void deleteProxyProvider(Long providerId) {
        var delete = new ServiceProvidersController()
                .withAdminToken()
                .deleteProviderById(providerId);
        new ProxyProviderAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);
    }

    @Step("Create regular proxy provider resource {proxyProvider.serviceName} {proxyProviderResource.title}")
    public void createProxyResource(ProxyProvider proxyProvider, ProxyProviderResource proxyProviderResource) {
        var responseRes = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(proxyProvider.getId(), proxyProviderResource);
        new ProxyProviderAssertion(responseRes)
                .expectedCode(HttpStatus.SC_OK);
        Long resId = responseRes.getBody().jsonPath().getLong("resources[0].id");
        String resHrn = responseRes.getBody().jsonPath().getString("resources[0].hrn");
        proxyProviderResource.setId(resId);
        proxyProviderResource.setHrn(resHrn);
    }

    @Step("Delete proxy resource {resourceId}")
    public void deleteProxyResource(Long resourceId) {
        var delete = new ServiceProvidersController()
                .withAdminToken()
                .deleteResourceFromProvider(resourceId);
        new ProxyProviderAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);
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

    @Step("Create Listing and Subscription for resource {proxyProviderResource.title}")
    public void createListingAndSubscription(ProxyProviderResource resource) {
        if (Conf.proxy().getMarketplaceMock()) {
            if (!"prod".equalsIgnoreCase(System.getProperty("env"))) {
                new ProxyAAController().createResourcePermission(resource.getHrn());
            }
        } else {
            MarketplaceSteps marketplaceSteps = new MarketplaceSteps();
            String listingHrn = marketplaceSteps.createNewProxyListing(resource.getHrn());
            marketplaceSteps.subscribeListing(listingHrn);
        }
    }

    @Step("Create Listing and Subscription for resource {proxyProviderResource.title}")
    public void createAndRemoveListingAndSubscription(ProxyProviderResource resource) {
        if (Conf.proxy().getMarketplaceMock()) {
            if (!"prod".equalsIgnoreCase(System.getProperty("env"))) {
                new ProxyAAController().wipeAllPolicies(resource.getHrn());
            }
        } else {
            MarketplaceSteps marketplaceSteps = new MarketplaceSteps();
            String listingHrn = marketplaceSteps.createNewProxyListing(resource.getHrn());
            String subsId = marketplaceSteps.subscribeListing(listingHrn);
            marketplaceSteps.beginCancellation(subsId);
            marketplaceSteps.deleteListing(listingHrn);
            if (!"prod".equalsIgnoreCase(System.getProperty("env"))) {
                new ProxyAAController().wipeAllPolicies(resource.getHrn());
            }
        }
    }
}
