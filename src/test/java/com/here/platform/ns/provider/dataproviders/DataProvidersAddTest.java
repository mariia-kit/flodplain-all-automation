package com.here.platform.ns.provider.dataproviders;

import static com.here.platform.ns.dto.Users.EXTERNAL_USER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.provider.ProviderController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.helpers.DefaultResponses;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
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

        var response = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(provider, "Provider content not as expected!");
    }

    @Test
    @DisplayName("Verify create new DataProvider with empty Token")
    void verifyDataProviderCreationWithEmptyToken() {
        DataProvider provider = Providers.generateNew();

        var response = new ProviderController()
                .withToken(StringUtils.EMPTY)
                .addProvider(provider);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_UNAUTHORIZED)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify create new DataProvider with invalid Token")
    void verifyDataProviderCreationWithInvalidToken() {
        DataProvider provider = Providers.generateNew();

        var response = new ProviderController()
                .withToken(EXTERNAL_USER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_FORBIDDEN)
                .expectedSentryError(SentryErrorsList.FORBIDDEN.getError());
    }

    @Test
    @DisplayName("Verify create new DataProvider empty body")
    void verifyDataProviderCreationInvalid() {
        DataProvider provider = Providers.generateNew();
        provider.setUrl(null);

        var response = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_BAD_REQUEST)
                .expectedError(NSErrors.getProviderInvalidError("url"));

        var verify = new ProviderController()
                .withToken(PROVIDER)
                .getProviderList();
        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> !DefaultResponses.isDataProviderPresentInList(provider, res),
                        "Provider should not be created!");
    }

    @Test
    @DisplayName("Verify create new DataProvider invalid Name")
    void verifyDataProviderCreationEmptyName() {
        DataProvider provider = Providers.generateNew();
        provider.setName(StringUtils.EMPTY);

        var response = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_METHOD_NOT_ALLOWED)
                .expectedError(NSErrors.getInvalidRequestMethod("PUT"));
    }

    @Test
    @DisplayName("Verify create new DataProvider invalid Url")
    void verifyDataProviderCreationEmptyUrl() {
        DataProvider provider = Providers.generateNew();
        provider.setUrl(StringUtils.EMPTY);

        var response = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_BAD_REQUEST)
                .expectedError(NSErrors.getProviderInvalidError("url"));

        var verify = new ProviderController()
                .withToken(PROVIDER)
                .getProviderList();
        new NeutralServerResponseAssertion(verify)
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

        var response = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(provider, "Provider content not as expected!");

        var verify = new ProviderController()
                .withToken(PROVIDER)
                .getProviderList();
        new NeutralServerResponseAssertion(verify)
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

        var response = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(provider, "Provider content not as expected!");

        var verify = new ProviderController()
                .withToken(PROVIDER)
                .getProviderList();
        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> DefaultResponses.isDataProviderPresentInList(provider, res),
                        "Provider " + provider.getName() + "should be present!");
    }


    @Test
    @DisplayName("Verify create new DataProvider invalid long Name")
    void verifyDataProviderCreationInvalidName() {
        DataProvider provider = Providers.generateNew();
        provider.setName(provider.getName() + StringUtils
                .repeat("ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx", 3));

        var response = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getProviderDataManipulationError(provider));
    }

}
