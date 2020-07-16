package com.here.platform.ns.provider.dataproviders;

import static com.here.platform.ns.dto.Users.EXTERNAL_USER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.helpers.DefaultResponses;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.restEndPoints.provider.data_providers.AddDataProviderCall;
import com.here.platform.ns.restEndPoints.provider.data_providers.DeleteDataProviderCall;
import com.here.platform.ns.restEndPoints.provider.data_providers.GetDataProvidersListCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Verify deletion of DataProvider")
public class DataProvidersDeleteTest extends BaseNSTest {

    @Test
    @DisplayName("Verify deletion of DataProvider Successful")
    void verifyDeleteDataProviders() {
        DataProvider provider = Providers.generateNew();
        Steps.createRegularProvider(provider);
        Steps.clearProviderResources(provider);

        new DeleteDataProviderCall(provider.getName())
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);
        new GetDataProvidersListCall()
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> !DefaultResponses.isDataProviderPresentInList(provider, res),
                        "Provider " + provider.getName() + "should not be present!");
    }

    @Test
    @DisplayName("Verify deletion of DataProvider empty Token")
    void verifyDeleteDataProvidersEmptyToken() {
        DataProvider provider = Providers.generateNew();
        Steps.createRegularProvider(provider);

        new DeleteDataProviderCall(provider.getName())
                .withToken(StringUtils.EMPTY)
                .call()
                .expectedCode(HttpStatus.SC_UNAUTHORIZED)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND);

        new GetDataProvidersListCall()
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> DefaultResponses.isDataProviderPresentInList(provider, res),
                        "Provider " + provider.getName() + "should be present!");
    }

    @Test
    @DisplayName("Verify deletion of DataProvider with invalid Token")
    void verifyDeleteDataProvidersInvalidToken() {
        DataProvider provider = Providers.generateNew();
        Steps.createRegularProvider(provider);

        new DeleteDataProviderCall(provider.getName())
                .withToken(EXTERNAL_USER)
                .call()
                .expectedSentryError(SentryErrorsList.FORBIDDEN);

        new GetDataProvidersListCall()
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> DefaultResponses.isDataProviderPresentInList(provider, res),
                        "Provider " + provider.getName() + "should be present!");
    }

    @Test
    @DisplayName("Verify deletion of DataProvider with Container")
    void verifyDeleteDataProvidersBindContainer() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);
        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new DeleteDataProviderCall(provider.getName())
                .withToken(PROVIDER)
                .call()
                .expectedError(NSErrors.getCouldntDeleteProviderError(provider.getName(),
                        container.getName()));
        new GetDataProvidersListCall()
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> DefaultResponses.isDataProviderPresentInList(provider, res),
                        "Provider " + provider.getName() + "should be present!");
    }

    @Test
    @DisplayName("Verify deletion of DataProvider with deleted Container")
    void verifyDeleteDataProvidersUnBindContainer() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);
        Steps.removeRegularContainer(container);
        Steps.clearProviderResources(provider);

        new DeleteDataProviderCall(provider.getName())
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);
        new GetDataProvidersListCall()
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> !DefaultResponses.isDataProviderPresentInList(provider, res),
                        "Provider " + provider.getName() + "should not be present!");
    }

    @Test
    @DisplayName("Verify deletion of DataProvider which not exist")
    void verifyDeleteDataProvidersNotExist() {
        DataProvider provider = Providers.generateNew();

        new DeleteDataProviderCall(provider.getName())
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_NOT_FOUND)
                .expectedError(NSErrors.getProviderNotFoundError(provider));
    }

    @Test
    @DisplayName("Verify deletion of DataProvider already Deleted")
    void verifyDeleteDataProvidersAlreadyDeleted() {
        DataProvider provider = Providers.generateNew();
        Steps.createRegularProvider(provider);
        Steps.clearProviderResources(provider);

        new DeleteDataProviderCall(provider.getName())
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);
        new DeleteDataProviderCall(provider.getName())
                .withToken(PROVIDER)
                .call()
                .expectedError(NSErrors.getProviderNotFoundError(provider));
    }

    @Test
    @DisplayName("Verify deletion of DataProvider with no resource")
    void verifyDeleteDataProvidersNoResource() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new DeleteDataProviderCall(provider.getName())
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);
        new GetDataProvidersListCall()
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> !DefaultResponses.isDataProviderPresentInList(provider, res),
                        "Provider " + provider.getName() + "should not be present!");
    }

}
