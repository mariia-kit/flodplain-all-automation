package com.here.platform.ns.provider.dataproviders;

import static com.here.platform.ns.dto.Users.EXTERNAL_USER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.DefaultResponses;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.restEndPoints.provider.data_providers.AddDataProviderCall;
import com.here.platform.ns.restEndPoints.provider.data_providers.GetDataProvidersListCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Verify creating new DataProvider")
class DataProvidersAddTest extends BaseNSTest {

    @Test
    @DisplayName("Verify create new DataProvider Successful")
    @Tag("smoke_ns")
    void verifyDataProviderCanBeCreated() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(provider, "Provider content not as expected!");
    }

    @Test
    @DisplayName("Verify create new DataProvider with empty Token")
    void verifyDataProviderCreationWithEmptyToken() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(StringUtils.EMPTY)
                .call()
                .expectedCode(HttpStatus.SC_UNAUTHORIZED)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify create new DataProvider with invalid Token")
    void verifyDataProviderCreationWithInvalidToken() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(EXTERNAL_USER)
                .call()
                .expectedCode(HttpStatus.SC_UNAUTHORIZED)
                .expectedSentryError(SentryErrorsList.TOKEN_INVALID.getError());
    }

    @Test
    @DisplayName("Verify create new DataProvider empty body")
    void verifyDataProviderCreationInvalid() {
        DataProvider provider = Providers.generateNew();
        provider.setUrl(null);
        new AddDataProviderCall(provider)
                .withToken(Users.PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_BAD_REQUEST)
                .expectedError(NSErrors.getProviderInvalidError("url"));

        new GetDataProvidersListCall()
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> !DefaultResponses.isDataProviderPresentInList(provider, res),
                        "Provider should not be created!");
    }

    @Test
    @DisplayName("Verify create new DataProvider invalid Name")
    void verifyDataProviderCreationEmptyName() {
        DataProvider provider = Providers.generateNew();
        provider.setName(StringUtils.EMPTY);
        new AddDataProviderCall(provider)
                .withToken(Users.PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_METHOD_NOT_ALLOWED)
                .expectedError(NSErrors.getInvalidRequestMethod("PUT"));
    }

    @Test
    @DisplayName("Verify create new DataProvider invalid Url")
    void verifyDataProviderCreationEmptyUrl() {
        DataProvider provider = Providers.generateNew();
        provider.setUrl(StringUtils.EMPTY);
        new AddDataProviderCall(provider)
                .withToken(Users.PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_BAD_REQUEST)
                .expectedError(NSErrors.getProviderInvalidError("url"));

        new GetDataProvidersListCall()
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> !DefaultResponses.isDataProviderPresentInList(provider, res),
                        "Provider should not be created!");
    }

    @Test
    @DisplayName("Verify create new DataProvider if already exist")
    void verifyDataProviderCreationAlreadyExist() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new AddDataProviderCall(provider)
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(provider, "Provider content not as expected!");

        new GetDataProvidersListCall()
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> DefaultResponses.isDataProviderPresentInList(provider, res),
                        "Provider " + provider.getName() + "should be present!");
    }

    @Test
    @DisplayName("Verify create new DataProvider if already exist with update")
    void verifyAddNewContainerAlreadyExistUpdate() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        provider.setUrl("ftp://my.com");
        new AddDataProviderCall(provider)
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(provider, "Provider content not as expected!");

        new GetDataProvidersListCall()
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> DefaultResponses.isDataProviderPresentInList(provider, res),
                        "Provider " + provider.getName() + "should be present!");
    }


    @Test
    @DisplayName("Verify create new DataProvider invalid Name long")
    void verifyDataProviderCreationInvalidName() {
        DataProvider provider = Providers.generateNew();
        provider.setName(provider.getName() + StringUtils
                .repeat("ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx", 3));
        new AddDataProviderCall(provider)
                .withToken(Users.PROVIDER)
                .call()
                .expectedError(NSErrors.getProviderDataManipulationError(provider));
    }

    @Test
    @DisplayName("Verify create new DataProvider invalid Name Symbol")
    void verifyDataProviderCreationInvalidNameSymbol() {
        DataProvider provider = Providers.generateNew();
        provider.setName(provider.getName() + ":abc/123");
        new AddDataProviderCall(provider)
                .withToken(Users.PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_NOT_FOUND);

    }

}