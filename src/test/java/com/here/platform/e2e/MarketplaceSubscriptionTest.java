package com.here.platform.e2e;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.ConsentManagerHelper;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.ConsentAfterCleanUp;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.external.MarketplaceManageListingCall;
import com.here.platform.ns.restEndPoints.neutralServer.resources.GetContainerDataByVehicleCall;
import com.here.platform.ns.restEndPoints.provider.container_info.AddContainerCall;
import com.here.platform.ns.restEndPoints.provider.container_info.DeleteContainerCall;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@DisplayName("Marketplace integration Tests: 'Subscription'")
@ExtendWith({MarketAfterCleanUp.class, ConsentAfterCleanUp.class})
@Tag("ignored-dev")
public class MarketplaceSubscriptionTest extends BaseE2ETest {

    @Test
    @DisplayName("Verify Subscription Successful")
    void verifySubscription() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.REF_DAIMLER_ODOMETER.getContainer();

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(crid)
                .call()
                .expectedCode(HttpStatus.SC_OK);

    }

    @Test
    @DisplayName("Verify Subscription create List without Container")
    void verifySubscriptionCreateListWithoutContainer() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        new MarketplaceManageListingCall()
                .createListing(container, "here-dev")
                .expectedCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("Verify Subscription create List for deleted Container")
    void verifySubscriptionCreateListingForDeletedContainer() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);

        new DeleteContainerCall(container)
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        new MarketplaceManageListingCall()
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

        new DeleteContainerCall(container)
                .call()
                .expectedError(NSErrors.getCantDeleteContainerWithSubs(container));
    }

    @Test
    @DisplayName("Verify delete Container with Listing no Subscription")
    void verifyDeleteContainerNoSubscription() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListing(container);

        new DeleteContainerCall(container)
                .call()
                .expectedError(NSErrors.getCantDeleteContainerWithSubs(container));
    }

    @Test
    @DisplayName("Verify delete Container with Subs In Progress")
    void verifyDeleteContainerSubscriptionInProgress() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscriptionInProgress(container);

        new DeleteContainerCall(container)
                .call()
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
        new AddContainerCall(container)
                .call()
                .expectedError(NSErrors.getCantEditContainerWithSubs(container));
    }

    @Test
    @DisplayName("Verify delete Container with Subs after cancel")
    void verifyDeleteContainerSubscriptionAfterSubsCancel() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscriptionRemoved(container);

        new DeleteContainerCall(container)
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("Verify get Container data with Listing no Subscription")
    void verifyGetContainerNoSubscription() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListing(container);

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(ConsentManagerHelper.getValidConsentId())
                .call()
                .expectedSentryError(SentryErrorsList.FORBIDDEN);
    }

    @Test
    @DisplayName("Verify get Container data after Subs cancel")
    void verifyGetContainerAfterSubsCancel() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscriptionRemoved(container);

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(ConsentManagerHelper.getValidConsentId())
                .call()
                .expectedCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("Verify get Container data with Subs In Progress")
    void verifyGetContainerSubscriptionInProgress() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscriptionInProgress(container);

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(ConsentManagerHelper.getValidConsentId())
                .call()
                .expectedSentryError(SentryErrorsList.FORBIDDEN);
    }

}
