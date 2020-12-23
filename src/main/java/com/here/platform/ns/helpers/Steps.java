package com.here.platform.ns.helpers;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.steps.remove.ConsentCollector;
import com.here.platform.common.config.Conf;
import com.here.platform.ns.controllers.access.ContainerDataController;
import com.here.platform.ns.controllers.provider.ContainerController;
import com.here.platform.ns.controllers.provider.ProviderController;
import com.here.platform.ns.controllers.provider.ResourceController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.ns.restEndPoints.external.AaaCall;
import com.here.platform.ns.restEndPoints.external.MarketplaceManageListingCall;
import com.here.platform.ns.restEndPoints.external.ReferenceProviderCall;
import io.qameta.allure.Step;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;


public class Steps {

    @Step("Create regular Data Provider: {provider.name}")
    public static void createRegularProvider(DataProvider provider) {
        String token = PROVIDER.getToken();
        var response = new ProviderController()
                .withBearerToken(token)
                .addProvider(provider);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK);
        List<ProviderResource> resource = new CopyOnWriteArrayList<>(provider.getResources());
        resource.forEach(res ->
                new ResourceController()
                        .withBearerToken(token)
                        .addResource(provider, res)
        );
    }

    @Step("Add resource {res.name} to Data Provider: {provider.name}")
    public static void addResourceToProvider(DataProvider provider, ProviderResource res) {
        var response = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        Assertions.assertTrue(
                response.getStatusCode() == HttpStatus.SC_OK || response.getStatusCode() == HttpStatus.SC_CONFLICT,
                "Creation of resource was not successful! " + response.getBody().print());
    }

    @Step("Remove resources of Data Provider: {provider.name}")
    public static void clearProviderResources(DataProvider provider) {
        provider.getResources().forEach(res -> {
                    var response = new ResourceController()
                            .withToken(PROVIDER)
                            .deleteResource(provider.getName(), res.getName());
                    new NeutralServerResponseAssertion(response)
                            .expectedCode(HttpStatus.SC_NO_CONTENT);
                }
        );
    }

    @Step("Remove regular Data Provider: {provider.name}")
    public static void removeRegularProvider(DataProvider provider) {
        clearProviderResources(provider);
        var response = new ProviderController()
                .withToken(PROVIDER)
                .deleteProvider(provider);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_NO_CONTENT);
    }

    @Step("Create regular Container {container.name} of provider {container.dataProviderName}")
    public static void createRegularContainer(Container container) {
        if (Stream.of(Providers.DAIMLER_REFERENCE, Providers.REFERENCE_PROVIDER, Providers.REFERENCE_PROVIDER_PROD,
                Providers.BMW_TEST)
                .anyMatch(prov -> prov.getProvider().getName().equals(container.getDataProviderName()))) {
            ReferenceProviderCall.createContainer(container);
        }
        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        CleanUpHelper.getContainersList().add(container.getId());
        ConsentCollector.addNSContainer(container);
        if ((response.getStatusCode() != HttpStatus.SC_OK) && (response.getStatusCode() != HttpStatus.SC_CONFLICT)) {
            throw new RuntimeException(
                    "Error creating container:" + response.getStatusCode() + " " + response.asString());
        }
    }

    @Step("Create regular Container for CM {container.id} for provider {container.provider.name}")
    public static void createRegularContainer(ConsentRequestContainer container) {
        Container cont = new Container(
                container.getId(),
                container.getName(),
                container.getProvider().getName(),
                container.getContainerDescription(),
                String.join(",", container.getResources()),
                true,
                container.getScopeValue());
        createRegularContainer(cont);
    }

    @Step("Remove regular Container {container.name} of provider {container.dataProviderName}")
    public static void removeRegularContainer(Container container) {
        var response = new ContainerController()
                .withToken(PROVIDER)
                .deleteContainer(container);
        new NeutralServerResponseAssertion(response)
                .expected(resp -> resp.getStatusCode() == HttpStatus.SC_NO_CONTENT
                                || resp.getStatusCode() == HttpStatus.SC_NOT_FOUND,
                        "Container deletion result not as expected");
    }

    @Step("Create regular Listing for {container.name}")
    public static void createListing(Container container) {
        new MarketplaceManageListingCall()
                .createNewListing(container);
    }

    @Step("Create regular Listing and Subscription for {container.name}")
    public static void createListingAndSubscription(Container container) {
        if (!Conf.ns().isMarketplaceMock()) {
            String listing = new MarketplaceManageListingCall()
                    .createNewListing(container);
            new MarketplaceManageListingCall()
                    .subscribeListing(listing);
            //new AaaCall().waitForContainerPolicyIntegrationInSentry(container.getDataProviderName(), container.getName());
        } else {
            new AaaCall().createResourcePermission(container);
        }
    }

    @Step("Create regular Listing and Canceled Subscription for {container.name}")
    public static void createListingAndCanceledSubscription(Container container) {
        String listing = new MarketplaceManageListingCall()
                .createNewListing(container);
        String subscription = new MarketplaceManageListingCall()
                .subscribeListing(listing);
        new MarketplaceManageListingCall().beginCancellation(subscription);
    }

    @Step("Create regular Listing and Subscription in progress for {container.name}")
    public static void createListingAndSubscriptionInProgress(Container container) {
        String listing = new MarketplaceManageListingCall()
                .createNewListing(container);
        String subscription = new MarketplaceManageListingCall()
                .subscribeListing(listing);
        new MarketplaceManageListingCall().beginCancellation(subscription);
    }

    @Step("Create and Remove regular Listing and Subscription for {container.name}")
    public static void createListingAndSubscriptionRemoved(Container container) {
        String listing = new MarketplaceManageListingCall()
                .createNewListing(container);
        String subsId = new MarketplaceManageListingCall()
                .subscribeListing(listing);
        new MarketplaceManageListingCall()
                .beginCancellation(subsId);
        new MarketplaceManageListingCall()
                .deleteListing(listing)
                .expectedCode(HttpStatus.SC_NO_CONTENT);
    }

    @Step("Get vehicle resources by Data Consumer from Data Provider")
    public static void getVehicleResourceAndVerify(String crid, String vin, Container container) {
        var response = new ContainerDataController()
                .withBearerToken(Users.MP_CONSUMER.getToken())
                .withConsentId(crid)
                .getContainerForVehicle(
                        container.getDataProviderByName(),
                        vin,
                        container
                );
        new NeutralServerResponseAssertion(response).expectedCode(200);
    }

}
