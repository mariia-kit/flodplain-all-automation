package com.here.platform.ns.provider.containersInfo;

import static com.here.platform.ns.dto.Users.EXTERNAL_USER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.provider.ContainerController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
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


@DisplayName("Verify creation of Container")
class ContainersInfoAddTest extends BaseNSTest {

    @Test
    @Tag("smoke_ns")
    @DisplayName("Verify create of Container Successful")
    void verifyAddNewContainer() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsContainer(container, "Container content not as expected!");
    }

    @Test
    @DisplayName("Verify create of Container with empty Token")
    void verifyAddNewContainerNoToken() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);

        var response = new ContainerController()
                .withToken(StringUtils.EMPTY)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify create of Container with invalid Token")
    void verifyAddNewContainerInvalidToken() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        var response = new ContainerController()
                .withToken(EXTERNAL_USER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedSentryError(SentryErrorsList.FORBIDDEN.getError());
    }

    @Test
    @DisplayName("Verify create of Container with no Provider exist")
    void verifyAddNewContainerNoProvider() {
        DataProvider provider = new DataProvider("no_such_provider_name", "Http://dot.com");
        Container container = Containers.generateNew(provider);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getProviderNotFoundError(provider));
    }

    @Test
    @DisplayName("Verify create of Container if already exist")
    void verifyAddNewContainerAlreadyExist() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsContainer(container, "Container content not as expected!");
        var data = new ContainerController()
                .withToken(PROVIDER)
                .getContainer(container);
        new NeutralServerResponseAssertion(data)
                .expectedEqualsContainer(container, "Container content not as expected!");
    }

    @Test
    @DisplayName("Verify create of Container empty description")
    void verifyAddNewContainerEmptyDescription() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider)
                .withDescription(StringUtils.EMPTY);

        Steps.createRegularProvider(provider);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getContainerInvalidFieldError("description"));

        var data = new ContainerController()
                .withToken(PROVIDER)
                .getContainer(container);
        new NeutralServerResponseAssertion(data)
                .expectedError(NSErrors.getContainersNotFoundError(container));
    }

    @Test
    @DisplayName("Verify create of Container false Consent Req flag.")
    void verifyAddNewContainerEmptyConsentFalse() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider)
                .withConsentRequired(false);

        Steps.createRegularProvider(provider);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsContainer(container, "Container content not as expected!");
        var data = new ContainerController()
                .withToken(PROVIDER)
                .getContainer(container);
        new NeutralServerResponseAssertion(data)
                .expectedEqualsContainer(container, "Container content not as expected!");
    }

    @Test
    @DisplayName("Verify create of Container empty Consent Req flag.")
    void verifyAddNewContainerEmptyConsentReq() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider)
                .withConsentRequired(null);

        Steps.createRegularProvider(provider);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK);
        container.withConsentRequired(true);
        var data = new ContainerController()
                .withToken(PROVIDER)
                .getContainer(container);
        new NeutralServerResponseAssertion(data)
                .expectedEqualsContainer(container, "Container content not as expected!");
    }

    @Test
    @DisplayName("Verify create of Container with Scope.")
    void verifyAddNewContainerSetScope() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider)
                .withConsentRequired(false)
                .withScope("general:some_scope");

        Steps.createRegularProvider(provider);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsContainer(container, "Container content not as expected!");
        var data = new ContainerController()
                .withToken(PROVIDER)
                .getContainer(container);
        new NeutralServerResponseAssertion(data)
                .expectedEqualsContainer(container, "Container content not as expected!");
    }

    @Test
    @DisplayName("Verify create of Container with long Scope.")
    void verifyAddNewContainerSetScopeLong() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider)
                .withConsentRequired(false)
                .withScope("general:some_scope:" + StringUtils
                        .repeat("ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx", 4));

        Steps.createRegularProvider(provider);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsContainer(container, "Container content not as expected!");
        var data = new ContainerController()
                .withToken(PROVIDER)
                .getContainer(container);
        new NeutralServerResponseAssertion(data)
                .expectedEqualsContainer(container, "Container content not as expected!");
    }

    @Test
    @DisplayName("Verify create of Container missing description")
    void verifyAddNewContainerMissingDescription() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider)
                .withDescription(null);

        Steps.createRegularProvider(provider);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getContainerInvalidFieldError("description"));
        var data = new ContainerController()
                .withToken(PROVIDER)
                .getContainer(container);
        new NeutralServerResponseAssertion(data)
                .expectedError(NSErrors.getContainersNotFoundError(container));
    }

    @Test
    @DisplayName("Verify create of Container with no name")
    void verifyAddNewContainerNoName() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider)
                .withName(null);
        Container expected = container.clone().withName(container.getId());

        Steps.createRegularProvider(provider);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedEqualsContainer(expected, "Container content not as expected!");
    }

    @Test
    @DisplayName("Verify update of Container with no name")
    void verifyAddNewContainerSetNameToNull() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);
        Container container2 = container.clone()
                .withName(null);

        Steps.createRegularProvider(provider);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK);

        var response2 = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container2);
        container.setName(container.getId());
        new NeutralServerResponseAssertion(response2)
                .expectedEqualsContainer(container, "Container content not as expected!");
    }

    @Test
    @DisplayName("Verify create of Container if invalid body")
    void verifyAddNewContainerInvalidEntity() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider)
                .withResourceNames(StringUtils.EMPTY);

        Steps.createRegularProvider(provider);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_BAD_REQUEST)
                .expectedError(NSErrors.getContainerInvalidFieldError("resourceNames"));
        var data = new ContainerController()
                .withToken(PROVIDER)
                .getContainer(container);
        new NeutralServerResponseAssertion(data)
                .expectedError(NSErrors.getContainersNotFoundError(container));
    }

    @Test
    @DisplayName("Verify create of Multiple Containers for one Provider")
    void verifyAddMultipleNewContainers() {
        DataProvider provider = Providers.generateNew();
        Container container1 = Containers.generateNew(provider);
        Container container2 = Containers.generateNew(provider);
        Container container3 = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);

        Steps.createRegularContainer(container1);
        Steps.createRegularContainer(container2);
        Steps.createRegularContainer(container3);

        var data = new ContainerController()
                .withToken(PROVIDER)
                .getContainersList(provider.getName());
        new NeutralServerResponseAssertion(data)
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> DefaultResponses.extractAsList(res).size() == 3,
                        "Expected list should not be equals to 3!")
                .expected(
                        res -> DefaultResponses.isContainerPresentInList(container1.getId(), res),
                        "No expected container in result!")
                .expected(
                        res -> DefaultResponses.isContainerPresentInList(container2.getId(), res),
                        "No expected container in result!")
                .expected(
                        res -> DefaultResponses.isContainerPresentInList(container3.getId(), res),
                        "No expected container in result!");
    }

    @Test
    @DisplayName("Verify update of Container all fields")
    void verifyAddNewContainerUpdateId() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);
        Container container2 = Containers.generateNew(provider)
                .withId(container.getId())
                .withDescription("Updated " + container.getName())
                .withResourceNames("Updated_" + container.getResourceNames())
                .withConsentRequired(false);
        Container containerTotal = container2.clone()
                .withId(container.getId())
                .withName(container2.getName())
                .withScope(container2.getScope());

        Steps.createRegularProvider(provider);
        Steps.addResourceToProvider(provider, new ProviderResource("Updated_" + container.getResourceNames()));

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsContainer(container, "Container content not as expected!");

        var update = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container2);
        new NeutralServerResponseAssertion(update)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsContainer(containerTotal, "Container content not as expected!");

        var data = new ContainerController()
                .withToken(PROVIDER)
                .getContainer(container);
        new NeutralServerResponseAssertion(data)
                .expectedEqualsContainer(containerTotal, "Container content not as expected!");
    }

    @Test
    @DisplayName("Verify create of Container with invalid Id")
    void verifyAddNewContainerInvalidID() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);
        container.withId(container.getId() + StringUtils
                .repeat("ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx", 3));

        Steps.createRegularProvider(provider);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_BAD_REQUEST)
                .expectedError(NSErrors.getContainerFieldValidationError("id"));
    }

    @Test
    @DisplayName("Verify create of Container no such resource set into provider")
    void verifyAddNewContainerNoSuchResource() {
        DataProvider provider = Providers.generateNew();
        ProviderResource res = ProviderResource.generateNew();
        Steps.createRegularProvider(provider);
        Container container = Containers.generateNew(provider)
                .withResourceNames(res.getName());

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getResourceIsMissingInProviderError(res, provider));
    }

    @Test
    @DisplayName("Verify update of Container no such resource set into provider")
    void verifyUpdateContainerNoSuchResource() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider);
        ProviderResource res = ProviderResource.generateNew();

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        Container updatedContainer = container.clone()
                .withResourceNames(res.getName());

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(updatedContainer);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getResourceIsMissingInProviderError(res, provider));
    }

    //@Test
    @DisplayName("Verify create of Container No Body")
    void verifyAddNewContainerNoBody() {
        DataProvider provider = Providers.generateNew();
        Container container = Containers.generateNew(provider)
                .withResourceNames(StringUtils.EMPTY);

        Steps.createRegularProvider(provider);
        var response = new ContainerController()
                .withToken(PROVIDER)
                //.withBody(StringUtils.EMPTY)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getContainerIncorrectBodyError());

        var data = new ContainerController()
                .withToken(PROVIDER)
                .getContainer(container);
        new NeutralServerResponseAssertion(data)
                .expectedError(NSErrors.getContainersNotFoundError(container));
    }

    @Test
    @DisplayName("Verify receive Multiple Resource data Successful")
    void verifyGetContainersDataCombinationResource() {
        DataProvider provider = Providers.generateNew();
        Steps.createRegularProvider(provider);

        ProviderResource res = ProviderResource.generateNew();
        ProviderResource res2 = ProviderResource.generateNew();
        Container container = Containers.generateNew(provider)
                .withResourceNames(res.getName() + "," + res2.getName());

        Steps.addResourceToProvider(provider, res);
        Steps.addResourceToProvider(provider, res2);

        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK);

        var data = new ContainerController()
                .withToken(PROVIDER)
                .getContainer(container);
        new NeutralServerResponseAssertion(data)
                .expectedCode(HttpStatus.SC_OK);
    }

}
