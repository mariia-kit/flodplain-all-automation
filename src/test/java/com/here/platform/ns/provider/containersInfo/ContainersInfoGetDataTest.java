package com.here.platform.ns.provider.containersInfo;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.restEndPoints.provider.container_info.DeleteContainerCall;
import com.here.platform.ns.restEndPoints.provider.container_info.GetContainerDataCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Verify receive Containers data")
class ContainersInfoGetDataTest extends BaseNSTest {

    @Test
    @DisplayName("Verify receive Containers data Successful")
    void verifyGetContainersDataRetrieved() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new GetContainerDataCall(container)
                .withToken(Users.PROVIDER)
                .call()
                .expectedEqualsContainer(container, "Container content not as expected!");
    }

    @Test
    @DisplayName("Verify receive Containers data with no Token")
    void verifyGetContainersNoToken() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new GetContainerDataCall(container)
                .withToken(StringUtils.EMPTY)
                .call()
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify receive Containers data with invalid Token")
    void verifyGetContainersInvalidToken() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new GetContainerDataCall(container)
                .withToken(Users.EXTERNAL_USER)
                .call()
                .expectedSentryError(SentryErrorsList.FORBIDDEN.getError());
    }

    @Test
    @DisplayName("Verify receive Containers data already Deleted")
    void verifyGetContainersDataAlreadyDeleted() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new DeleteContainerCall(container)
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);
        new GetContainerDataCall(container)
                .call()
                .expectedError(NSErrors.getContainersNotFoundError(container));
    }

    @Test
    @DisplayName("Verify receive Containers data with not valid Provider")
    void verifyGetContainersDataNoProvider() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        container.withDataProviderName("no_such_provider");
        new GetContainerDataCall(container)
                .call()
                .expectedError(NSErrors.getContainersNotFoundError(container));
    }

    @Test
    @DisplayName("Verify receive Containers data with not valid Container")
    void verifyGetContainersDataNoContainer() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        container.withId("no_such_container");
        new GetContainerDataCall(container)
                .call()
                .expectedError(NSErrors.getContainersNotFoundError(container));
    }

}
