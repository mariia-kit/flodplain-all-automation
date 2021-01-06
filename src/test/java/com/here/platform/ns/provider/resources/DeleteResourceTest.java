package com.here.platform.ns.provider.resources;

import static com.here.platform.ns.dto.Users.CONSUMER;
import static com.here.platform.ns.dto.Users.EXTERNAL_USER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.access.VehicleResourceController;
import com.here.platform.ns.controllers.provider.ProviderController;
import com.here.platform.ns.controllers.provider.ResourceController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.ContainerResources;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.DefaultResponses;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.ns.restEndPoints.external.AaaCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@DisplayName("Verify deletion of ContainerResources of Data Provider")
@ExtendWith({MarketAfterCleanUp.class})
public class DeleteResourceTest extends BaseNSTest {

    @Test
    @DisplayName("Verify deletion of Resources Successful")
    void verifyDeleteResources() {
        DataProvider provider = Providers.generateNew();
        ProviderResource res = ProviderResource.generateNew();

        var create = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(create)
                .expectedCode(HttpStatus.SC_OK);

        var add = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(add)
                .expectedCode(HttpStatus.SC_OK);

        var delete = new ResourceController()
                .withToken(PROVIDER)
                .deleteResource(provider, res);
        new NeutralServerResponseAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        var verify = new ResourceController()
                .withToken(PROVIDER)
                .getResourceList(provider);
        new NeutralServerResponseAssertion(verify)
                .expected(DefaultResponses::isResponseListEmpty,
                        "Expected list should be empty!")
                .expected(response -> !DefaultResponses
                                .isResourceInList(res, response),
                        "Not expected resource is in result!");
    }

    @Test
    @DisplayName("Verify deletion of Resources no Token")
    void verifyDeleteResourcesNoToken() {
        DataProvider provider = Providers.generateNew();
        ProviderResource res = ProviderResource.generateNew();

        var create = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(create)
                .expectedCode(HttpStatus.SC_OK);

        var add = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(add)
                .expectedCode(HttpStatus.SC_OK);

        var delete = new ResourceController()
                .withToken(StringUtils.EMPTY)
                .deleteResource(provider, res);
        new NeutralServerResponseAssertion(delete)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify deletion of Resources invalid Token")
    void verifyDeleteResourcesInvalidToken() {
        DataProvider provider = Providers.generateNew();
        ProviderResource res = ProviderResource.generateNew();

        var create = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(create)
                .expectedCode(HttpStatus.SC_OK);

        var add = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(add)
                .expectedCode(HttpStatus.SC_OK);

        var delete = new ResourceController()
                .withToken(EXTERNAL_USER)
                .deleteResource(provider, res);
        new NeutralServerResponseAssertion(delete)
                .expectedSentryError(SentryErrorsList.FORBIDDEN.getError());
    }

    @Test
    @DisplayName("Verify deletion of Resources for not exist DataProvider")
    void verifyDeleteResourcesInvalidProvider() {
        DataProvider provider = Providers.generateNew();
        ProviderResource res = ProviderResource.generateNew();

        var create = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(create)
                .expectedCode(HttpStatus.SC_OK);

        var add = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(add)
                .expectedCode(HttpStatus.SC_OK);

        provider.setName("no_such_provider");
        var delete = new ResourceController()
                .withToken(PROVIDER)
                .deleteResource(provider, res);
        new NeutralServerResponseAssertion(delete)
                .expectedError(NSErrors.getProviderNotFoundError(provider));
    }

    @Test
    @DisplayName("Verify deletion of not exist Resources from DataProvider")
    void verifyDeleteResourcesInvalidResources() {
        DataProvider provider = Providers.generateNew();
        ProviderResource res = ProviderResource.generateNew();

        var create = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(create)
                .expectedCode(HttpStatus.SC_OK);

        var add = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(add)
                .expectedCode(HttpStatus.SC_OK);

        res.setName("no_such_container");
        var delete = new ResourceController()
                .withToken(PROVIDER)
                .deleteResource(provider, res);
        new NeutralServerResponseAssertion(delete)
                .expectedError(NSErrors.getProviderResourceNotFoundError(provider.getName(),
                        res.getName()));
    }

    @Test
    @DisplayName("Verify deletion of Resources alien Provider")
    void verifyDeleteResourcesAlienContainer() {
        DataProvider provider = Providers.generateNew();
        DataProvider provider2 = Providers.generateNew();
        ProviderResource res = ProviderResource.generateNew();

        var create = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(create)
                .expectedCode(HttpStatus.SC_OK);
        var create2 = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider2);
        new NeutralServerResponseAssertion(create2)
                .expectedCode(HttpStatus.SC_OK);

        var add = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(add)
                .expectedCode(HttpStatus.SC_OK);

        var delete = new ResourceController()
                .withToken(PROVIDER)
                .deleteResource(provider2, res);
        new NeutralServerResponseAssertion(delete)
                .expectedError(NSErrors.getProviderResourceNotFoundError(provider2.getName(),
                        res.getName()));
    }

    @Test
    @DisplayName("Verify deletion of Resources already Deleted")
    void verifyDeleteResourcesAlreadyDeleted() {
        DataProvider provider = Providers.generateNew();
        ProviderResource res = ProviderResource.generateNew();

        var create = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(create)
                .expectedCode(HttpStatus.SC_OK);

        var add = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(add)
                .expectedCode(HttpStatus.SC_OK);
        var delete = new ResourceController()
                .withToken(PROVIDER)
                .deleteResource(provider, res);
        new NeutralServerResponseAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);
        var delete2 = new ResourceController()
                .withToken(PROVIDER)
                .deleteResource(provider, res);
        new NeutralServerResponseAssertion(delete2)
                .expectedError(NSErrors.getProviderResourceNotFoundError(provider.getName(),
                        res.getName()));
    }

    @Test
    @DisplayName("Verify deletion of Resources if Multiple exist")
    void verifyDeleteResourcesIfMultipleExist() {
        DataProvider provider = Providers.generateNew();
        ProviderResource res1 = ProviderResource.generateNew();
        ProviderResource res2 = ProviderResource.generateNew();

        var create = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(create)
                .expectedCode(HttpStatus.SC_OK);

        var add1 = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res1);
        new NeutralServerResponseAssertion(add1)
                .expectedCode(HttpStatus.SC_OK);
        var add2 = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res2);
        new NeutralServerResponseAssertion(add2)
                .expectedCode(HttpStatus.SC_OK);

        var delete = new ResourceController()
                .withToken(PROVIDER)
                .deleteResource(provider, res1);
        new NeutralServerResponseAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        var verify = new ResourceController()
                .withToken(PROVIDER)
                .getResourceList(provider);
        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expected(response -> !DefaultResponses.isResponseListEmpty(response),
                        "Expected list should not be empty!")
                .expected(response -> !DefaultResponses
                                .isResourceInList(res1, response),
                        "Not expected resource is in result!")
                .expected(response -> DefaultResponses
                                .isResourceInList(res2, response),
                        "No expected resource in result!");
    }

    @Test
    @DisplayName("Verify deletion of Resources if same name exist")
    void verifyDeleteResourcesIfSameNameExist() {
        DataProvider provider1 = Providers.generateNew();
        DataProvider provider2 = Providers.generateNew();
        ProviderResource res1 = ProviderResource.generateNew();
        ProviderResource res2 = ProviderResource.generateNew();

        var create1 = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider1);
        new NeutralServerResponseAssertion(create1)
                .expectedCode(HttpStatus.SC_OK);
        var create2 = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider2);
        new NeutralServerResponseAssertion(create2)
                .expectedCode(HttpStatus.SC_OK);

        var add1 = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider1, res1);
        new NeutralServerResponseAssertion(add1)
                .expectedCode(HttpStatus.SC_OK);
        var add2 = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider2, res2);
        new NeutralServerResponseAssertion(add2)
                .expectedCode(HttpStatus.SC_OK);

        var delete = new ResourceController()
                .withToken(PROVIDER)
                .deleteResource(provider1, res1);
        new NeutralServerResponseAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        var verify1 = new ResourceController()
                .withToken(PROVIDER)
                .getResourceList(provider1);
        new NeutralServerResponseAssertion(verify1)
                .expected(DefaultResponses::isResponseListEmpty,
                        "Expected list should be empty!");

        var verify2 = new ResourceController()
                .withToken(PROVIDER)
                .getResourceList(provider2);
        new NeutralServerResponseAssertion(verify2)
                .expected(response -> DefaultResponses.extractAsList(response).size() == 1,
                        "Expected list should not be empty!")
                .expected(response -> DefaultResponses
                                .isResourceInList(res2, response),
                        "No expected resource in result!");
    }

    @Test
    @DisplayName("Verify deletion of Resources that is used in Container")
    void verifyDeleteResourcesUsedInContainer() {
        ProviderResource res = ProviderResource.generateNew();
        DataProvider provider = Providers.generateNew()
                .withResources(res);
        Container container = Containers.generateNew(provider)
                .withResourceNames(res.getName());

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        var delete = new ResourceController()
                .withToken(PROVIDER)
                .deleteResource(provider, res);
        new NeutralServerResponseAssertion(delete)
                .expectedError(NSErrors.getResourceCantBeDeletedError(res, container));

        var verify = new ResourceController()
                .withToken(PROVIDER)
                .getResourceList(provider);
        new NeutralServerResponseAssertion(verify)
                .expected(response -> DefaultResponses
                                .isResourceInList(res, response),
                        "No expected resource is in result! " + res.getName());
    }

    @Test
    @DisplayName("Verify deletion of Resources that is used in Container Capital")
    void verifyDeleteResourcesUsedInContainerCapital() {
        ProviderResource res1 = ProviderResource.generateNew();
        ProviderResource res2 = new ProviderResource(res1.getName().toUpperCase());
        DataProvider provider = Providers.generateNew()
                .withResources(res1)
                .withResources(res2);
        Container container = Containers.generateNew(provider)
                .withResourceNames(res1.getName());

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        var delete = new ResourceController()
                .withToken(PROVIDER)
                .deleteResource(provider, res2);
        new NeutralServerResponseAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        var verify = new ResourceController()
                .withToken(PROVIDER)
                .getResourceList(provider);
        new NeutralServerResponseAssertion(verify)
                .expected(response -> !DefaultResponses
                                .isResourceInList(res2, response),
                        "Not expected resource is in result! " + res2.getName());
    }

    @Test
    @DisplayName("Verify delete resource with subscription")
    void verifyDeleteResourcesWithSubs() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        ProviderResource res1 = ContainerResources.FUEL.getResource();
        Container container = Containers.generateNew(provider)
                .withResourceNames(res1.getName())
                .withConsentRequired(false);

        Steps.createRegularProvider(provider);
        Steps.addResourceToProvider(Providers.REFERENCE_PROVIDER.getProvider(), res1);
        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithRes(container, res1);

        var getSingle1 = new VehicleResourceController()
                .withToken(CONSUMER)
                .getVehicleResource(provider, Vehicle.validVehicleId, res1);
        new NeutralServerResponseAssertion(getSingle1)
                .expectedCode(HttpStatus.SC_OK);

        var delete = new ResourceController()
                .withToken(PROVIDER)
                .deleteResource(provider, res1);
        new NeutralServerResponseAssertion(delete)
                .expectedError(NSErrors.getCantDeleteResourceWithSubs(res1, provider));
    }


}
