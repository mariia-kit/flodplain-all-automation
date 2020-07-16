package com.here.platform.ns.provider.resources;

import static com.here.platform.ns.dto.Users.EXTERNAL_USER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.helpers.DefaultResponses;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.restEndPoints.provider.data_providers.AddDataProviderCall;
import com.here.platform.ns.restEndPoints.provider.resources.AddProviderResourceCall;
import com.here.platform.ns.restEndPoints.provider.resources.GetProviderResourceCall;
import com.here.platform.ns.restEndPoints.provider.resources.GetResourcesCall;
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
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new GetProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("name", res.getName(), "Provider resource not as expected!");
    }

    @Test
    @DisplayName("Verify create new ContainerResources DataProvider with empty Token")
    void verifyDataProviderResourcesWithEmptyToken() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        new AddProviderResourceCall(provider, res.getName())
                .withToken(StringUtils.EMPTY)
                .call()
                .expectedCode(HttpStatus.SC_UNAUTHORIZED)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify create new ContainerResources DataProvider with invalid Token")
    void verifyDataProviderResourcesWithWrongToken() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        new AddProviderResourceCall(provider, res.getName())
                .withToken(EXTERNAL_USER)
                .call()
                .expectedSentryError(SentryErrorsList.FORBIDDEN.getError());
    }

    @Test
    @DisplayName("Verify create new ContainerResources DataProvider Already exist")
    void verifyDataProviderResourcesAlreadyExist() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);
        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new GetProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("name", res.getName(), "Provider resource not as expected!");

        new GetResourcesCall(provider)
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(resp -> DefaultResponses.extractAsList(resp).size() == 1,
                        "Expected list should be with size 1!");
    }

    @Test
    @DisplayName("Verify create new ContainerResources DataProvider Multiple")
    void verifyDataProviderResourcesCanBeCreatedMultiple() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        ProviderResource res1 = ProviderResource.generateNew();
        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);
        new AddProviderResourceCall(provider, res1.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new GetProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("name", res.getName(), "Provider resource not as expected!");
        new GetProviderResourceCall(provider, res1.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("name", res1.getName(), "Provider resource not as expected!");

        new GetResourcesCall(provider)
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(resp -> DefaultResponses.extractAsList(resp).size() == 2,
                        "Expected list should be with size 2!");
    }

    @Test
    @DisplayName("Verify create of Resource with no Provider exist")
    void verifyDataProviderResourcesNoProvider() {
        DataProvider provider = new DataProvider("no_such_provider_name", "ppp");
        ProviderResource res = ProviderResource.generateNew();
        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedError(NSErrors.getProviderNotFoundError(provider));
    }

    @Test
    @DisplayName("Verify create new ContainerResources DataProvider Empty ID")
    void verifyDataProviderResourcesEmptyID() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = new ProviderResource(StringUtils.EMPTY);
        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedError(NSErrors.getInvalidRequestMethod("PUT"));
    }

    @Test
    @DisplayName("Verify create new ContainerResources DataProvider Long ID")
    void verifyDataProviderResourcesLongId() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);
        ProviderResource res = ProviderResource.generateNew();
        res.setName(
                res.getName() + StringUtils.repeat("ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx", 10));
        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedError(NSErrors.getResourceDataManipulationError(res, provider));

        new GetResourcesCall(provider)
                .call()
                .expected(DefaultResponses::isResponseListEmpty,
                        "Expected list should be empty!");
    }

}
