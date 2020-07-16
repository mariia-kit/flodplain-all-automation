package com.here.platform.ns.access.vehicleResources;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.ConsentManagerHelper;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.ConsentAfterCleanUp;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.neutralServer.resources.GetContainerDataByVehicleCall;
import io.restassured.http.Header;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@DisplayName("Get resources by vehicle Id and container Id")
@ExtendWith({MarketAfterCleanUp.class, ConsentAfterCleanUp.class})
class GetResourcesByVehicleAndContainerTest extends BaseNSTest {

    @Test
    //@Tag("smoke_ns")
    @DisplayName("Verify get resources by vehicle Id and container Id Successful")
    void verifyGetContainersDataRetrieved() {
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
                .expectedEqualsContainerData(
                        Vehicle.odometerResource,
                        "Provider content not as expected!");
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id and container Id bad token")
    void verifyGetContainersDataRetrievedBadToken() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withToken(Users.EXTERNAL_USER)
                .withCampaignId(ConsentManagerHelper.getValidConsentId())
                .call()
                .expectedSentryError(SentryErrorsList.FORBIDDEN);
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id and container Id token")
    void verifyGetContainersDataRetrievedBadWrong() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withToken(Users.PROVIDER)
                .withCampaignId(ConsentManagerHelper.getValidConsentId())
                .call()
                .expectedSentryError(SentryErrorsList.FORBIDDEN);
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id and container Id: Charge")
    void verifyGetContainersDataRetrievedResourceCharge() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.REF_DAIMLER_CHARGE.getContainer();

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(ConsentManagerHelper.getValidConsentId())
                .call()
                .expectedEqualsContainerData(Vehicle.chargeResource,
                        "Provider content not as expected!");
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id and container Id: Invalid Resource")
    void verifyGetContainersDataRetrievedResourceInvalid() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(ConsentManagerHelper.getValidConsentId())
                .call()
                .expectedCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id and container Id: Capital")
    void verifyGetContainersDataRetrievedResourceCapital() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        new GetContainerDataByVehicleCall(Providers.DAIMLER_CAPITAL.getProvider().getName(),
                Vehicle.validVehicleId, container.getId())
                .withCampaignId(ConsentManagerHelper.getValidConsentId())
                .call()
                .expectedSentryError(SentryErrorsList.FORBIDDEN);
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id and container Id: Invalid Vehicle")
    @Tag("ignored-dev")
    void verifyGetContainersDataRetrievedResourceInvalidVehicle() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);
        String crid = ConsentManagerHelper.getValidConsentId();

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.invalidVehicleId,
                container.getId())
                .withCampaignId(crid)
                .call()
                .expectedError(NSErrors.getCMInvalidVehicleError(crid));
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id and container Id with X-Corr Id")
    void verifyGetContainersDataXCorrId() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.REF_DAIMLER_ODOMETER.getContainer();

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(ConsentManagerHelper.getValidConsentId())
                .withHeader("X-Correlation-ID", "X-corr-1")
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedHeader(new Header("X-Correlation-ID", "X-corr-1"));

    }

    @Test
    @Tag("ignored-dev")
    @DisplayName("Verify get resources by vehicle Id and container Id with no Campaign Id")
    void verifyGetContainersDataNoCampaignId() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .call()
                .expectedCode(HttpStatus.SC_NOT_FOUND)
                .expectedHeaderIsPresent("X-Correlation-ID");

    }

    @Test
    //@Tag("smoke_ns")
    @DisplayName("Verify get resources by vehicle Id and container Id for empty response")
    void verifyGetContainersDataRetrievedEmpty() {
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
                .withQueryParam("additional-fields=empty&additional-values=on")
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT)
                .expectedBody(StringUtils.EMPTY, "Expected empty body!");
    }

    @Test
    //@Tag("smoke_ns")
    @DisplayName("Verify get resources by vehicle Id and container Id for empty response Reference")
    void verifyGetContainersDataRetrievedEmptyReference() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        Container container = Containers.generateNew(provider);

        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);
        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withCampaignId(crid)
                .withQueryParam("additional-fields=empty&additional-values=on")
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT)
                .expectedBody(StringUtils.EMPTY, "Expected empty body!");
    }

}
