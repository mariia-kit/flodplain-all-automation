package com.here.platform.ns.provider.resources;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.ContainerResources;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.DefaultResponses;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.ConsentAfterCleanUp;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.external.AaaCall;
import com.here.platform.ns.restEndPoints.neutralServer.resources.GetSingleResourceByVehicleCall;
import com.here.platform.ns.restEndPoints.provider.data_providers.AddDataProviderCall;
import com.here.platform.ns.restEndPoints.provider.resources.AddProviderResourceCall;
import com.here.platform.ns.restEndPoints.provider.resources.DeleteProviderResourceCall;
import com.here.platform.ns.restEndPoints.provider.resources.GetResourcesCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@DisplayName("Verify deletion of ContainerResources of Data Provider")
@ExtendWith({MarketAfterCleanUp.class, ConsentAfterCleanUp.class})
public class DeleteResourceTest extends BaseNSTest {

    @Test
    @DisplayName("Verify deletion of ContainerResources Successful")
    void verifyDeleteResources() {
        DataProvider provider = Providers.generateNew();
        ProviderResource res = ProviderResource.generateNew();

        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new DeleteProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        new GetResourcesCall(provider)
                .withToken(PROVIDER)
                .call()
                .expected(DefaultResponses::isResponseListEmpty,
                        "Expected list should be empty!")
                .expected(response -> !DefaultResponses
                                .isResourceInList(res, response),
                        "Not expected resource is in result!");
    }

    @Test
    @DisplayName("Verify deletion of ContainerResources no Token")
    void verifyDeleteResourcesNoToken() {
        DataProvider provider = Providers.generateNew();
        ProviderResource res = ProviderResource.generateNew();

        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new DeleteProviderResourceCall(provider, res.getName())
                .withToken(StringUtils.EMPTY)
                .call()
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify deletion of ContainerResources invalid Token")
    void verifyDeleteResourcesInvalidToken() {
        DataProvider provider = Providers.generateNew();
        ProviderResource res = ProviderResource.generateNew();

        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new DeleteProviderResourceCall(provider, res.getName())
                .withToken(Users.EXTERNAL_USER)
                .call()
                .expectedSentryError(SentryErrorsList.TOKEN_INVALID.getError());
    }

    @Test
    @DisplayName("Verify deletion of ContainerResources invalid Provider")
    void verifyDeleteResourcesInvalidProvider() {
        DataProvider provider = Providers.generateNew();
        ProviderResource res = ProviderResource.generateNew();

        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        provider.setName("no_such_provider");
        new DeleteProviderResourceCall(provider, res.getName())
                .call()
                .expectedError(NSErrors.getProviderNotFoundError(provider));
    }

    @Test
    @DisplayName("Verify deletion of ContainerResources invalid ContainerResources")
    void verifyDeleteResourcesInvalidResources() {
        DataProvider provider = Providers.generateNew();
        ProviderResource res = ProviderResource.generateNew();

        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        res.setName("no_such_container");
        new DeleteProviderResourceCall(provider, res.getName())
                .call()
                .expectedError(NSErrors.getProviderResourceNotFoundError(provider.getName(),
                        res.getName()));
    }

    @Test
    @DisplayName("Verify deletion of ContainerResources alien Provider")
    void verifyDeleteResourcesAlienContainer() {
        DataProvider provider = Providers.generateNew();
        DataProvider provider2 = Providers.generateNew();
        ProviderResource res = ProviderResource.generateNew();

        new AddDataProviderCall(provider)
                .call();
        new AddDataProviderCall(provider2)
                .call();

        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new DeleteProviderResourceCall(provider2, res.getName())
                .call()
                .expectedError(NSErrors.getProviderResourceNotFoundError(provider2.getName(),
                        res.getName()));
    }

    @Test
    @DisplayName("Verify deletion of ContainerResources already Deleted")
    void verifyDeleteResourcesAlreadyDeleted() {
        DataProvider provider = Providers.generateNew();
        ProviderResource res = ProviderResource.generateNew();

        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);
        new DeleteProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);
        new DeleteProviderResourceCall(provider, res.getName())
                .call()
                .expectedError(NSErrors.getProviderResourceNotFoundError(provider.getName(),
                        res.getName()));
    }

    @Test
    @DisplayName("Verify deletion of ContainerResources if Multiple exist")
    void verifyDeleteResourcesIfMultipleExist() {
        DataProvider provider = Providers.generateNew();
        ProviderResource res1 = ProviderResource.generateNew();
        ProviderResource res2 = ProviderResource.generateNew();

        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new AddProviderResourceCall(provider, res1.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);
        new AddProviderResourceCall(provider, res2.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new DeleteProviderResourceCall(provider, res1.getName())
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        new GetResourcesCall(provider)
                .withToken(PROVIDER)
                .call()
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
    @DisplayName("Verify deletion of ContainerResources if same name exist")
    void verifyDeleteResourcesIfSameNameExist() {
        DataProvider provider1 = Providers.generateNew();
        DataProvider provider2 = Providers.generateNew();
        ProviderResource res1 = ProviderResource.generateNew();
        ProviderResource res2 = ProviderResource.generateNew();

        new AddDataProviderCall(provider1)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);
        new AddDataProviderCall(provider2)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new AddProviderResourceCall(provider1, res1.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);
        new AddProviderResourceCall(provider2, res2.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new DeleteProviderResourceCall(provider1, res1.getName())
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        new GetResourcesCall(provider1)
                .withToken(PROVIDER)
                .call()
                .expected(DefaultResponses::isResponseListEmpty,
                        "Expected list should be empty!");

        new GetResourcesCall(provider2)
                .withToken(PROVIDER)
                .call()
                .expected(response -> DefaultResponses.extractAsList(response).size() == 1,
                        "Expected list should not be empty!")
                .expected(response -> DefaultResponses
                                .isResourceInList(res2, response),
                        "No expected resource in result!");
    }

    @Test
    @DisplayName("Verify deletion of ContainerResources used in Container")
    void verifyDeleteResourcesUsedInContainer() {
        ProviderResource res = ProviderResource.generateNew();
        DataProvider provider = Providers.generateNew()
                .withResources(res);
        Container container = Containers.generateNew(provider)
                .withResourceNames(res.getName());

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new DeleteProviderResourceCall(provider, res.getName())
                .call()
                .expectedError(NSErrors.getResourceCantBeDeletedError(res, container));

        new GetResourcesCall(provider)
                .withToken(PROVIDER)
                .call()
                .expected(response -> DefaultResponses
                                .isResourceInList(res, response),
                        "No expected resource is in result! " + res.getName());
    }

    @Test
    @DisplayName("Verify deletion of ContainerResources used in Container Capital")
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

        new DeleteProviderResourceCall(provider, res2.getName())
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        new GetResourcesCall(provider)
                .withToken(PROVIDER)
                .call()
                .expected(response -> !DefaultResponses
                                .isResourceInList(res2, response),
                        "Not expected resource is in result! " + res2.getName());
    }

    @Test
    @DisplayName("Verify delete resource with subscription")
    void verifyDeleteResourcesWithSubs() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        Steps.createRegularProvider(provider);

        ProviderResource res1 = ContainerResources.FUEL.getResource();
        new AddProviderResourceCall(provider, res1.getName())
                .call();

        Container container = Containers.generateNew(provider)
                .withResourceNames(res1.getName())
                .withConsentRequired(false);

        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithRes(container, res1);

        new GetSingleResourceByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                res1.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new DeleteProviderResourceCall(provider, res1.getName())
                .call()
                .expectedError(NSErrors.getCantDeleteResourceWithSubs(res1, provider));
    }


}