package com.here.platform.ns.provider.resources;

import static com.here.platform.ns.dto.Users.EXTERNAL_USER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.provider.ProviderController;
import com.here.platform.ns.controllers.provider.ResourceController;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.helpers.DefaultResponses;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Verify creating new ContainerResources for DataProvider")
public class AddResourcesTest extends BaseNSTest {

    @Test
    @DisplayName("Verify create new ContainerResources DataProvider Successful")
    void verifyDataProviderResourcesCanBeCreated() {
        DataProvider provider = Providers.generateNew();

        var create = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(create)
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();

        var response = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK);

        var verify = new ResourceController()
                .withToken(PROVIDER)
                .getResource(provider, res.getName());
        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("name", res.getName(), "Provider resource not as expected!");
    }

    @Test
    @DisplayName("Verify create new ContainerResources DataProvider with empty Token")
    void verifyDataProviderResourcesWithEmptyToken() {
        DataProvider provider = Providers.generateNew();
        var create = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(create)
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();

        var response = new ResourceController()
                .withToken(StringUtils.EMPTY)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_UNAUTHORIZED)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify create new ContainerResources DataProvider with invalid Token")
    void verifyDataProviderResourcesWithWrongToken() {
        DataProvider provider = Providers.generateNew();
        var create = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(create)
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();

        var response = new ResourceController()
                .withToken(EXTERNAL_USER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(response)
                .expectedSentryError(SentryErrorsList.TOKEN_INVALID.getError());
    }

    @Test
    @DisplayName("Verify create new ContainerResources DataProvider Already exist")
    void verifyDataProviderResourcesAlreadyExist() {
        DataProvider provider = Providers.generateNew();
        var create = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(create)
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        var response1 = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(response1)
                .expectedCode(HttpStatus.SC_OK);
        var response2 = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(response2)
                .expectedCode(HttpStatus.SC_OK);

        var verify = new ResourceController()
                .withToken(PROVIDER)
                .getResource(provider, res.getName());
        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("name", res.getName(), "Provider resource not as expected!");

        var verifyAll = new ResourceController()
                .withToken(PROVIDER)
                .getResourceList(provider.getName());
        new NeutralServerResponseAssertion(verifyAll)
                .expectedCode(HttpStatus.SC_OK)
                .expected(resp -> DefaultResponses.extractAsList(resp).size() == 1,
                        "Expected list should be with size 1!");
    }

    @Test
    @DisplayName("Verify create new ContainerResources DataProvider Multiple")
    void verifyDataProviderResourcesCanBeCreatedMultiple() {
        DataProvider provider = Providers.generateNew();
        var addDataProvider = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(addDataProvider)
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        ProviderResource res1 = ProviderResource.generateNew();
        var response1 = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(response1)
                .expectedCode(HttpStatus.SC_OK);
        var response2 = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res1);
        new NeutralServerResponseAssertion(response2)
                .expectedCode(HttpStatus.SC_OK);

        var verify1 = new ResourceController()
                .withToken(PROVIDER)
                .getResource(provider, res.getName());
        new NeutralServerResponseAssertion(verify1)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("name", res.getName(), "Provider resource not as expected!");
        var verify2 = new ResourceController()
                .withToken(PROVIDER)
                .getResource(provider, res1.getName());
        new NeutralServerResponseAssertion(verify2)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("name", res1.getName(), "Provider resource not as expected!");

        var verifyAll = new ResourceController()
                .withToken(PROVIDER)
                .getResourceList(provider.getName());
        new NeutralServerResponseAssertion(verifyAll)
                .expectedCode(HttpStatus.SC_OK)
                .expected(resp -> DefaultResponses.extractAsList(resp).size() == 2,
                        "Expected list should be with size 2!");
    }

    @Test
    @DisplayName("Verify create of Resource with no Provider exist")
    void verifyDataProviderResourcesNoProvider() {
        DataProvider provider = new DataProvider("no_such_provider_name", "ppp");
        ProviderResource res = ProviderResource.generateNew();
        var response = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getProviderNotFoundError(provider));
    }

    @Test
    @DisplayName("Verify create new ContainerResources DataProvider Empty ID")
    void verifyDataProviderResourcesEmptyID() {
        DataProvider provider = Providers.generateNew();
        var create = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(create)
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = new ProviderResource(StringUtils.EMPTY);
        var response = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getInvalidRequestMethod("PUT"));
    }

    @Test
    @DisplayName("Verify create new ContainerResources DataProvider Long ID")
    void verifyDataProviderResourcesLongId() {
        DataProvider provider = Providers.generateNew();
        var create = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(create)
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        res.setName(res.getName() + StringUtils.repeat("ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx", 10));

        var response = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getResourceDataManipulationError(res, provider));

        var verifyAll = new ResourceController()
                .withToken(PROVIDER)
                .getResourceList(provider.getName());
        new NeutralServerResponseAssertion(verifyAll)
                .expected(DefaultResponses::isResponseListEmpty,
                        "Expected list should be empty!");
    }

}
