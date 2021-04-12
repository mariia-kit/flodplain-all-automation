package com.here.platform.ns.provider.containersInfo;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.provider.ContainerController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("NS-ContainerInfo")
@Disabled
@DisplayName("Verify receive Containers data")
class ContainersInfoGetDataTest extends BaseNSTest {

    @Test
    @DisplayName("Verify receive Containers data Successful")
    void verifyGetContainersDataRetrieved() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .getContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedEqualsContainer(container, "Container content not as expected!");
    }

    @Test
    @DisplayName("Verify receive Containers data with no Token")
    void verifyGetContainersNoToken() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        var response = new ContainerController()
                .withToken(StringUtils.EMPTY)
                .deleteContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify receive Containers data with invalid Token")
    void verifyGetContainersInvalidToken() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        var response = new ContainerController()
                .withToken("Bearer 12345")
                .getContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedSentryError(SentryErrorsList.TOKEN_CORRUPTED.getError());
    }

    @Test
    @DisplayName("Verify receive Containers data already Deleted")
    void verifyGetContainersDataAlreadyDeleted() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .deleteContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        var verify = new ContainerController()
                .withToken(PROVIDER)
                .getContainer(container);
        new NeutralServerResponseAssertion(verify)
                .expectedError(NSErrors.getContainersNotFoundError(container));
    }

    @Test
    @DisplayName("Verify receive Containers data with not valid Provider")
    void verifyGetContainersDataNoProvider() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        Container container2 = container.clone().withDataProviderId("no_such_provider");

        var response = new ContainerController()
                .withToken(PROVIDER)
                .deleteContainer(container2);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getContainersNotFoundError(container2));
    }

    @Test
    @DisplayName("Verify receive Containers data with not valid Container")
    void verifyGetContainersDataNoContainer() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        Container container2 = container.clone().withId("no_such_container");

        var response = new ContainerController()
                .withToken(PROVIDER)
                .deleteContainer(container2);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getContainersNotFoundError(container2));
    }

}
