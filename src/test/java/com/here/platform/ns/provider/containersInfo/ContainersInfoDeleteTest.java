package com.here.platform.ns.provider.containersInfo;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.provider.ContainerController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.DefaultResponses;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("NS-ContainerInfo")
@DisplayName("Verify deletion of Container")
class ContainersInfoDeleteTest extends BaseNSTest {

    @Test
    @DisplayName("Verify deletion of Container Successful")
    void verifyDeleteContainer() {
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
    @DisplayName("Verify deletion of Container no Token")
    void verifyDeleteContainerNoToken() {
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
    @DisplayName("Verify deletion of Container invalid Token")
    void verifyDeleteContainerInvalidToken() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        var response = new ContainerController()
                .withToken(Users.EXTERNAL_USER)
                .deleteContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedSentryError(SentryErrorsList.FORBIDDEN.getError());
    }

    @Test
    @DisplayName("Verify deletion of Container invalid Provider")
    void verifyDeleteContainerInvalidProvider() {
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
    @DisplayName("Verify deletion of Container invalid Container")
    void verifyDeleteContainerInvalidContainer() {
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

    @Test
    @DisplayName("Verify deletion of Container alien Container")
    void verifyDeleteContainerAlienContainer() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);
        DataProvider provider2 = Providers.generateNew();

        Steps.createRegularProvider(provider);
        Steps.createRegularProvider(provider2);
        Steps.createRegularContainer(container);

        Container container2 = container.clone().withDataProviderId(provider2.getId());

        var response = new ContainerController()
                .withToken(PROVIDER)
                .deleteContainer(container2);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getContainersNotFoundError(container2));
    }

    @Test
    @DisplayName("Verify deletion of Container already Deleted")
    void verifyDeleteContainerAlreadyDeleted() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .deleteContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        var secondResponse = new ContainerController()
                .withToken(PROVIDER)
                .deleteContainer(container);
        new NeutralServerResponseAssertion(secondResponse)
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

        var response = new ContainerController()
                .withToken(PROVIDER)
                .deleteContainer(container1);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        var verify = new ContainerController()
                .withToken(PROVIDER)
                .getContainersList(provider.getId());
        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> DefaultResponses.extractAsList(res).size() == 1,
                        "Expected list should not be equals to 1!")
                .expected(
                        res -> DefaultResponses.isContainerPresentInList(container2.getId(), res),
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
                .withDataProviderId(provider2.getId());

        Steps.createRegularProvider(provider1);
        Steps.createRegularProvider(provider2);

        Steps.createRegularContainer(container1);
        Steps.createRegularContainer(container2);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .deleteContainer(container1);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        var verify = new ContainerController()
                .withToken(PROVIDER)
                .getContainersList(provider1.getId());
        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_NOT_FOUND)
                .expectedError(NSErrors.getContainersForProviderNotFoundError(provider1.getId()));

        var verify2 = new ContainerController()
                .withToken(PROVIDER)
                .getContainersList(provider2.getId());
        new NeutralServerResponseAssertion(verify2)
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> DefaultResponses.extractAsList(res).size() == 1,
                        "Expected list should not be equals to 1!")
                .expected(
                        res -> DefaultResponses.isContainerPresentInList(container2.getId(), res),
                        "No expected container in result!");
    }

}
