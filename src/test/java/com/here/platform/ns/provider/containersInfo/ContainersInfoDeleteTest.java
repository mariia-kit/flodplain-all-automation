package com.here.platform.ns.provider.containersInfo;

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
import com.here.platform.ns.restEndPoints.provider.container_info.DeleteContainerCall;
import com.here.platform.ns.restEndPoints.provider.container_info.GetContainerDataCall;
import com.here.platform.ns.restEndPoints.provider.container_info.GetContainersListForProviderCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Verify deletion of Container")
class ContainersInfoDeleteTest extends BaseNSTest {

    @Test
    @DisplayName("Verify deletion of Container Successful")
    void verifyDeleteContainer() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new DeleteContainerCall(container)
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);
        new GetContainerDataCall(container.getName(), container.getDataProviderName())
                .call()
                .expectedError(NSErrors.getContainersNotFoundError(container));
    }

    @Test
    @DisplayName("Verify deletion of Container no Token")
    void verifyDeleteContainerNoToken() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new DeleteContainerCall(container)
                .withToken(StringUtils.EMPTY)
                .call()
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify deletion of Container invalid Token")
    void verifyDeleteContainerInvalidToken() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new DeleteContainerCall(container)
                .withToken(Users.EXTERNAL_USER)
                .call()
                .expectedSentryError(SentryErrorsList.TOKEN_INVALID.getError());
    }

    @Test
    @DisplayName("Verify deletion of Container invalid Provider")
    void verifyDeleteContainerInvalidProvider() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        container.setDataProviderName("no_such_provider");
        new DeleteContainerCall(container)
                .call()
                .expectedError(NSErrors.getContainersNotFoundError(container));
    }

    @Test
    @DisplayName("Verify deletion of Container invalid Container")
    void verifyDeleteContainerInvalidContainer() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        container.setId("no_such_container");
        new DeleteContainerCall(container)
                .call()
                .expectedError(NSErrors.getContainersNotFoundError(container));
    }

    @Test
    @DisplayName("Verify deletion of Container alien Container")
    void verifyDeleteContainerAlienContainer() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);
        DataProvider provider2 = Providers.generateNew();

        Steps.createRegularProvider(provider);
        Steps.createRegularProvider(provider2);
        Steps.createRegularContainer(container);

        container.setDataProviderName(provider2.getName());
        new DeleteContainerCall(container)
                .call()
                .expectedError(NSErrors.getContainersNotFoundError(container));
    }

    @Test
    @DisplayName("Verify deletion of Container already Deleted")
    void verifyDeleteContainerAlreadyDeleted() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new DeleteContainerCall(container)
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);
        new DeleteContainerCall(container)
                .call()
                .expectedError(NSErrors.getContainersNotFoundError(container));
    }

    @Test
    @DisplayName("Verify deletion of Container if Multiple exist")
    void verifyDeleteContainerIfMultipleExist() {
        DataProvider provider = Providers.generateNew();
        Container container1 = Containers.generateNew(provider);
        Container container2 = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);

        Steps.createRegularContainer(container1);
        Steps.createRegularContainer(container2);

        new DeleteContainerCall(container1).call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        new GetContainersListForProviderCall(provider.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> DefaultResponses.extractAsList(res).size() == 1,
                        "Expected list should not be equals to 1!")
                .expected(
                        res -> DefaultResponses.isContainerPresentInList(container2.getName(), res),
                        "No expected container in result!");
    }

    @Test
    @DisplayName("Verify deletion of Container if same name exist")
    void verifyDeleteContainerIfSameNameExist() {
        DataProvider provider1 = Providers.generateNew();
        DataProvider provider2 = Providers.generateNew();
        Container container1 = Containers.generateNew(provider1);
        Container container2 = container1
                .clone()
                .withDataProviderName(provider2.getName());

        Steps.createRegularProvider(provider1);
        Steps.createRegularProvider(provider2);

        Steps.createRegularContainer(container1);
        Steps.createRegularContainer(container2);

        new DeleteContainerCall(container1).call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        new GetContainersListForProviderCall(provider1.getName())
                .call()
                .expectedCode(HttpStatus.SC_NOT_FOUND)
                .expectedError(NSErrors.getContainersForProviderNotFoundError(provider1.getName()));

        new GetContainersListForProviderCall(provider2.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> DefaultResponses.extractAsList(res).size() == 1,
                        "Expected list should not be equals to 1!")
                .expected(
                        res -> DefaultResponses.isContainerPresentInList(container2.getName(), res),
                        "No expected container in result!");
    }

}
