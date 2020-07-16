package com.here.platform.ns.provider.resources;

import static com.here.platform.ns.dto.Users.EXTERNAL_USER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.helpers.DefaultResponses;
import com.here.platform.ns.restEndPoints.provider.data_providers.AddDataProviderCall;
import com.here.platform.ns.restEndPoints.provider.resources.AddProviderResourceCall;
import com.here.platform.ns.restEndPoints.provider.resources.GetResourcesCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Verify get all ContainerResources for DataProvider")
public class GetResourceListTest extends BaseNSTest {

    @Test
    @DisplayName("Verify receive list of DataProvider ContainerResources Successful")
    void verifyGetResourceListCanBeRetrieved() {
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

        new GetResourcesCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(response -> !DefaultResponses.isResponseListEmpty(response),
                        "Expected list should not be empty!")
                .expected(response -> DefaultResponses
                                .isResourceInList(res, response),
                        "No expected resource in result!")
                .expected(response -> DefaultResponses
                                .isResourceInList(res1, response),
                        "No expected resource in result!");
    }

    @Test
    @DisplayName("Verify receive list of DataProvider ContainerResources with empty Token")
    void verifyGetResourceListWithEmptyToken() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();

        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new GetResourcesCall(provider)
                .withToken(StringUtils.EMPTY)
                .call()
                .expectedCode(HttpStatus.SC_UNAUTHORIZED)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify receive list of DataProvider ContainerResources with invalid Token")
    void verifyGetResourceListWithWrongToken() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();

        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new GetResourcesCall(provider)
                .withToken(EXTERNAL_USER)
                .call()
                .expectedSentryError(SentryErrorsList.FORBIDDEN.getError());
    }

}
