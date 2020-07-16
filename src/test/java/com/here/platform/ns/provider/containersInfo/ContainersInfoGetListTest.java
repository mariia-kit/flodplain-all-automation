package com.here.platform.ns.provider.containersInfo;

import static com.here.platform.ns.dto.Providers.DAIMLER_REAL;
import static com.here.platform.ns.dto.Providers.DAIMLER_REFERENCE;
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
import com.here.platform.ns.restEndPoints.provider.container_info.DeleteContainerCall;
import com.here.platform.ns.restEndPoints.provider.container_info.GetContainersListForProviderCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Verify receive list of Containers")
class ContainersInfoGetListTest extends BaseNSTest {

    @Test
    @DisplayName("Verify receive list of Containers Successful")
    @Tag("smoke_ns")
    void verifyGetContainersListCanBeRetrieved() {
        new GetContainersListForProviderCall(DAIMLER_REAL.getName())
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> !DefaultResponses.isResponseListEmpty(res),
                        "Expected list should not be empty!")
                .expected(res -> DefaultResponses.isContainerPresentInList("payasyoudrive", res),
                        "No expected container in result!");
    }

    @Test
    @DisplayName("Verify receive list of Containers with empty Provider")
    void verifyGetContainersListForEmptyProvider() {
        DataProvider provider = Providers.generateNew();

        Steps.createRegularProvider(provider);

        new GetContainersListForProviderCall(provider.getName())
                .call()
                .expectedCode(HttpStatus.SC_NOT_FOUND)
                .expectedError(NSErrors.getContainersForProviderNotFoundError(provider.getName()));
    }

    @Test
    @DisplayName("Verify receive list of Containers with empty Token")
    void verifyGetContainersListWithEmptyToken() {
        DataProvider provider = Providers.generateNew();

        Steps.createRegularProvider(provider);

        new GetContainersListForProviderCall(provider.getName())
                .withToken(StringUtils.EMPTY)
                .call()
                .expectedCode(HttpStatus.SC_UNAUTHORIZED)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify receive list of Containers with invalid Token")
    void verifyGetContainersListWithWrongToken() {
        new GetContainersListForProviderCall(DAIMLER_REFERENCE.getName())
                .withToken(EXTERNAL_USER)
                .call()
                .expectedSentryError(SentryErrorsList.FORBIDDEN.getError());
    }

    @Test
    @DisplayName("Verify receive list of Containers with Provider not exist")
    void verifyGetContainersListWithNoProvider() {
        new GetContainersListForProviderCall("no_such_provider_name")
                .call()
                .expectedError(
                        NSErrors.getContainersForProviderNotFoundError("no_such_provider_name"));
    }

    @Test
    @DisplayName("Verify receive list of Containers case sensitive")
    void verifyGetContainersListCaseSensitive() {
        DataProvider provider = Providers.generateNew();

        Steps.createRegularProvider(provider);

        new GetContainersListForProviderCall(provider.getName().toLowerCase())
                .call()
                .expectedError(
                        NSErrors.getContainersForProviderNotFoundError(
                                (provider.getName().toLowerCase())));
    }

    @Test
    @DisplayName("Verify receive list for Deleted Data Provider")
    void verifyGetContainersListDeletedProvider() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new DeleteContainerCall(container.getName(), container.getDataProviderName())
                .call();
        new GetContainersListForProviderCall(provider.getName())
                .call()
                .expectedError(
                        NSErrors.getContainersForProviderNotFoundError((provider.getName())));
    }

    @Test
    @DisplayName("Verify receive list with Container with Consent set to false")
    void verifyGetContainersListContainerWithConsent() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider)
                .withConsentRequired(false);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new GetContainersListForProviderCall(provider.getName())
                .call()
                .expected(res -> !DefaultResponses.isResponseListEmpty(res),
                        "Expected list should not be empty!")
                .expectedEqualsContainerInList(container, "No expected container in result!");
    }

    @Test
    @DisplayName("Verify receive list with Container with empty Consent")
    void verifyGetContainersListContainerWithEmptyConsent() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider)
                .withConsentRequired(null);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        container.setConsentRequired(true);
        new GetContainersListForProviderCall(provider.getName())
                .call()
                .expected(res -> !DefaultResponses.isResponseListEmpty(res),
                        "Expected list should not be empty!")
                .expectedEqualsContainerInList(container, "No expected container in result!");
    }

}
