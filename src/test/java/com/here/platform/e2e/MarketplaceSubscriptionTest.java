package com.here.platform.e2e;

import static com.here.platform.ns.dto.Users.CONSUMER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.ns.controllers.access.ContainerDataController;
import com.here.platform.ns.controllers.provider.ContainerController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.mp.steps.api.MarketplaceSteps;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@DisplayName("Marketplace integration Tests: 'Subscription'")
@Tag("ignored-dev")
public class MarketplaceSubscriptionTest extends BaseE2ETest {

    @Test
    @DisplayName("Verify Subscription Successful")
    void verifySubscription() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(Providers.DAIMLER_REFERENCE.getName());

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        ConsentObject consentObj = new ConsentObject(container);
        String testVin = Vehicle.validVehicleId;

        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin)
                .approveConsent(testVin)
                .getId();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK);

    }

    @Test
    @DisplayName("Verify Subscription create List without Container")
    void verifySubscriptionCreateListWithoutContainer() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        new MarketplaceSteps()
                .createListing(container, "here-dev")
                .expectedCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("Verify Subscription create List for deleted Container")
    void verifySubscriptionCreateListingForDeletedContainer() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);

        var delete = new ContainerController()
                .withToken(PROVIDER)
                .deleteContainer(container);
        new NeutralServerResponseAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        new MarketplaceSteps()
                .createListing(container, "here-dev")
                .expectedCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("Verify delete Container with Subscription")
    void verifyDeleteContainerSubscription() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        var delete = new ContainerController()
                .withToken(PROVIDER)
                .deleteContainer(container);
        new NeutralServerResponseAssertion(delete)
                .expectedError(NSErrors.getCantDeleteContainerWithSubs(container));
    }

    @Test
    @DisplayName("Verify delete Container with Listing no Subscription")
    void verifyDeleteContainerNoSubscription() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListing(container);

        var delete = new ContainerController()
                .withToken(PROVIDER)
                .deleteContainer(container);
        new NeutralServerResponseAssertion(delete)
                .expectedError(NSErrors.getCantDeleteContainerWithSubs(container));
    }

    @Test
    @DisplayName("Verify delete Container with Subs In Progress")
    void verifyDeleteContainerSubscriptionInProgress() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscriptionInProgress(container);

        var delete = new ContainerController()
                .withToken(PROVIDER)
                .deleteContainer(container);
        new NeutralServerResponseAssertion(delete)
                .expectedError(NSErrors.getCantDeleteContainerWithSubs(container));

    }

    @Test
    @DisplayName("Verify edit Container with Subs In Progress")
    void verifyEditContainerSubscriptionInProgress() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscriptionInProgress(container);

        container.withDescription("Edited description!");
        var response = new ContainerController()
                .withToken(PROVIDER)
                .addContainer(container);
        new NeutralServerResponseAssertion(response)
                .expectedError(NSErrors.getCantEditContainerWithSubs(container));
    }

    @Test
    @DisplayName("Verify delete Container with Subs after cancel")
    void verifyDeleteContainerSubscriptionAfterSubsCancel() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscriptionRemoved(container);

        var delete = new ContainerController()
                .withToken(PROVIDER)
                .deleteContainer(container);
        new NeutralServerResponseAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("Verify get Container data with Listing no Subscription")
    void verifyGetContainerNoSubscription() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListing(container);
        ConsentObject consentObj = new ConsentObject(container);
        String testVin = Vehicle.validVehicleId;

        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin)
                .approveConsent(testVin)
                .getId();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedSentryError(SentryErrorsList.FORBIDDEN);
    }

    @Test
    @DisplayName("Verify get Container data after Subs cancel")
    void verifyGetContainerAfterSubsCancel() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscriptionRemoved(container);

        ConsentObject consentObj = new ConsentObject(container);
        String testVin = Vehicle.validVehicleId;

        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin)
                .approveConsent(testVin)
                .getId();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("Verify get Container data with Subs In Progress")
    void verifyGetContainerSubscriptionInProgress() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscriptionInProgress(container);

        ConsentObject consentObj = new ConsentObject(container);
        String testVin = Vehicle.validVehicleId;

        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin)
                .approveConsent(testVin)
                .getId();

        var response = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response)
                .expectedSentryError(SentryErrorsList.FORBIDDEN);
    }

}
