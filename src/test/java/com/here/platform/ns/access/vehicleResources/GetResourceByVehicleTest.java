package com.here.platform.ns.access.vehicleResources;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.ContainerResources;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.ConsentManagerHelper;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.ConsentAfterCleanUp;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.external.AaaCall;
import com.here.platform.ns.restEndPoints.neutralServer.resources.GetContainerDataByVehicleCall;
import com.here.platform.ns.restEndPoints.neutralServer.resources.GetResourceAsyncResultCall;
import com.here.platform.ns.restEndPoints.neutralServer.resources.InitGetResourceAsyncCall;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@DisplayName("Get resources by vehicle Id Reference Provider Async")
@ExtendWith({MarketAfterCleanUp.class, ConsentAfterCleanUp.class})
public class GetResourceByVehicleTest extends BaseNSTest {

    @Test
    @DisplayName("Verify get resources by vehicle Id no-PII local")
    void verifyGetContainersDataRetrievedReferenceL() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        Container container = Containers.generateNew(provider)
                .withResourceNames("fuel")
                .withConsentRequired(false);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .call()
                .expectedCode(HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id Daimler Ref")
    void verifyGetContainersDataRetrievedDaimlerReference() {
        DataProvider provider = Providers.DAIMLER_REFERENCE.getProvider();
        Container container = Containers.REF_DAIMLER_ODOMETER.getContainer();

        Steps.createRegularProvider(provider);
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
    @DisplayName("Verify get resources Async by vehicle Id no-PII")
    void verifyGetContainersDataRetrievedReferenceLAsync() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        ProviderResource res1 = ContainerResources.CHARGE.getResource();
        provider.addResource(res1);
        Container container = Containers.generateNew(provider)
                .withResourceNames(res1.getName())
                .withConsentRequired(false);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithRes(container, res1);

        String location = new InitGetResourceAsyncCall(provider.getName(), Vehicle.validVehicleId,
                res1.getName())
                .call()
                .expectedCode(HttpStatus.SC_ACCEPTED)
                .expectedHeaderIsPresent("Location")
                .getResponse().getHeader("Location");
        new GetResourceAsyncResultCall(location)
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedJsonContains("resourceReadout.asyncStatus", "InProgress", "Not expected async State!");
        delay(5500L);
        new GetResourceAsyncResultCall(location)
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedJsonContains("resourceReadout.asyncStatus", "Complete", "Not expected async State!")
                .expectedJsonContains("resourceReadout.resources[0].stateofcharge.value", "0.8", "Not expected res vale!");
    }

    @Test
    @DisplayName("Verify get resources Async by vehicle Id Fast Response")
    void verifyGetContainersDataRetrievedReferenceFastLAsync() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        ProviderResource res1 = ContainerResources.FUEL.getResource();
        provider.addResource(res1);
        Container container = Containers.generateNew(provider)
                .withResourceNames(res1.getName())
                .withConsentRequired(false);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithRes(container, res1);

        new InitGetResourceAsyncCall(provider.getName(), Vehicle.validVehicleId,
                res1.getName())
                .call()
                .expectedCode(HttpStatus.SC_CREATED)
                .expectedJsonContains("resourceReadout.asyncStatus", "Complete", "Not expected async State!")
                .expectedJsonContains("resourceReadout.resources[0].rangeliquid.value", "1648", "Not expected res value!");
    }

    @Test
    @DisplayName("Verify get resources Async by vehicle Id Error Expected")
    void verifyGetContainersDataRetrievedReferenceFastLAsyncError() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        ProviderResource res1 = new ProviderResource("alastor");
        provider.addResource(res1);
        Container container = Containers.generateNew(provider)
                .withConsentRequired(false);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithRes(container, res1);

        new InitGetResourceAsyncCall(provider.getName(), Vehicle.validVehicleId,
                res1.getName())
                .call()
                .expectedCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Verify get resources Async by vehicle Id bad request id")
    void verifyGetContainersDataRetrievedReferenceLAsyncBadRequest() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        ProviderResource res1 = ContainerResources.CHARGE.getResource();
        provider.addResource(res1);
        Container container = Containers.generateNew(provider)
                .withResourceNames(res1.getName())
                .withConsentRequired(false);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithRes(container, res1);

        String location = new InitGetResourceAsyncCall(provider.getName(), Vehicle.validVehicleId,
                res1.getName())
                .call()
                .expectedCode(HttpStatus.SC_ACCEPTED)
                .expectedHeaderIsPresent("Location")
                .getResponse().getHeader("Location");
        new GetResourceAsyncResultCall(location + "some_bad_request_id")
                .call()
                .expectedCode(HttpStatus.SC_NOT_FOUND);

    }

}
