package com.here.platform.ns.access.vehicleResources;


import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.dto.*;
import com.here.platform.ns.dto.ContainerResources;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.ConsentManagerHelper;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.ConsentAfterCleanUp;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.external.AaaCall;
import com.here.platform.ns.restEndPoints.neutralServer.resources.GetAllResourcesByVehicleCall;
import com.here.platform.ns.restEndPoints.neutralServer.resources.GetContainerDataByVehicleCall;
import com.here.platform.ns.restEndPoints.neutralServer.resources.GetResourceAsyncResultCall;
import com.here.platform.ns.restEndPoints.neutralServer.resources.GetSingleResourceByVehicleCall;
import com.here.platform.ns.restEndPoints.neutralServer.resources.InitGetResourceAsyncCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@DisplayName("Get resources by vehicle Id Pagination, Filtering...")
@ExtendWith({MarketAfterCleanUp.class, ConsentAfterCleanUp.class})
public class GetResourcesNonDaimlerFunc extends BaseNSTest {

    @Test
    @DisplayName("Verify get resources by vehicle Id filter")
    void verifyGetContainersDataRetrieved() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        Container container = Containers.generateNew(provider).withResourceNames("odometer,location");

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        String crid = new ConsentManagerHelper(container, Vehicle.validVehicleId)
                .createConsentRequestWithAppAndVin()
                .approveConsent()
                .getConsentRequestId();

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withQueryParam("additional-fields=resource&additional-values=distancesincereset")
                .withCampaignId(crid)
                .call()
                .expectedEqualsISOContainerData(
                        Vehicle.getResourceMap(Vehicle.odometerResource, "distancesincereset"),
                        "Container data content not as expected!");

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withQueryParam("additional-fields=resourceName&additional-values=odometer")
                .withCampaignId(crid)
                .call()
                .expectedEqualsISOContainerData(
                        Vehicle.odometerResource,
                        "Container data content not as expected!");

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withQueryParam("additional-fields=resourceName&additional-values=notrealres")
                .withCampaignId(crid)
                .call()
                .expectedEqualsISOContainerData(
                        Vehicle.empty,
                        "Container data content not as expected!");
    }

    @Test
    @DisplayName("Verify get resources by vehicle Id Daimler filter")
    void verifyGetResourceDataRetrievedFilterDaimler() {
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
                .withQueryParam("additional-fields=resource&additional-values=distancesincereset")
                .withCampaignId(crid)
                .call()
                .expectedEqualsContainerData(
                        Vehicle.getResourceMap(Vehicle.odometerResource, "distancesincereset"),
                        "Container data content not as expected!");

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withQueryParam("additional-fields=resource&additional-values=odometer")
                .withCampaignId(crid)
                .call()
                .expectedEqualsContainerData(
                        Vehicle.getResourceMap(Vehicle.odometerResource, "odometer"),
                        "Container data content not as expected!");

        new GetContainerDataByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                container.getId())
                .withQueryParam("additional-fields=resource&additional-values=notrealres")
                .withCampaignId(crid)
                .call()
                .expectedEqualsContainerData(
                        Vehicle.empty,
                        "Container data content not as expected!");
    }

    @Test
    @DisplayName("Verify get single resource by vehicle Id filter")
    void verifyGetResourceRetrievedFilter() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        ProviderResource res1 = ContainerResources.ODOMETER.getResource();
        provider.addResource(res1);
        Steps.createRegularProvider(provider);

        Container container = Containers.generateNew(provider)
                .withResourceNames(res1.getName())
                .withConsentRequired(false);

        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithRes(container, res1);

        new GetSingleResourceByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                res1.getName())
                .withQueryParam("additional-fields=resource&additional-values=distancesincereset")
                .call()
                .expectedEqualsContainerData(
                        Vehicle.getResourceMap(Vehicle.odometerResource, "distancesincereset"),
                        "Container data content not as expected!");

        new GetSingleResourceByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                res1.getName())
                .withQueryParam("additional-fields=resource&additional-values=odometer")
                .call()
                .expectedEqualsContainerData(
                       Vehicle.getResourceMap(Vehicle.odometerResource, "odometer"),
                        "Resource data content not as expected!");

        new GetSingleResourceByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                res1.getName())
                .withQueryParam("additional-fields=resource&additional-values=notrealres")
                .call()
                .expectedEqualsContainerData(
                        Vehicle.empty,
                        "Resource data content not as expected!");
    }

    @Test
    @DisplayName("Verify init get single resource by vehicle Id filter Async")
    void verifyGetResourceDataRetrievedAsyncFastFilter() {
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
                .withQueryParam("additional-fields=resource&additional-values=rangeliquid")
                .call()
                .expectedJsonContains("resourceReadout.resources[0].rangeliquid.value", "1648",
                        "Not expected res value!");

        new InitGetResourceAsyncCall(provider.getName(), Vehicle.validVehicleId,
                res1.getName())
                .withQueryParam("additional-fields=resource&additional-values=notrealres")
                .call()
                .expectedJsonContains("resourceReadout.resources", "[]",
                        "Not expected res value!");
    }

    @Test
    @DisplayName("Verify get single resource by request Id filter Async")
    void verifyGetResourceDataRetrievedFilterAsync() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        ProviderResource res1 = ContainerResources.ODOMETER.getResource();
        provider.addResource(res1);
        Container container = Containers.generateNew(provider)
                .withResourceNames(res1.getName())
                .withConsentRequired(false);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithRes(container, res1);

        String location = new InitGetResourceAsyncCall(provider.getName(), Vehicle.validVehicleId,
                res1.getName())
                .withQueryParam("additional-fields=resource&additional-values=rangeliquid")
                .call()
                .expectedCode(HttpStatus.SC_ACCEPTED)
                .expectedHeaderIsPresent("Location")
                .getResponse().getHeader("Location");

        new GetResourceAsyncResultCall(location)
                .withQueryParam("additional-fields=resource&additional-values=rangeliquid")
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedJsonContains("resourceReadout.asyncStatus", "InProgress", "Not expected async State!");
        delay(5500L);
        new GetResourceAsyncResultCall(location)
                .withQueryParam("additional-fields=resource&additional-values=distancesincereset")
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedJsonContains("resourceReadout.asyncStatus", "Complete", "Not expected async State!")
                .expectedJsonContains("resourceReadout.resources[0].distancesincereset.value", "1234", "Not expected res vale!")
                .expectedJsonContains("resourceReadout.resources.size()", "1", "Not expected res value!");
    }

    @Test
    @DisplayName("Verify All ContainerResources by vehicle Id Filter")
    void verifyAllResourceRetrievedFilter() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        ProviderResource res1 = ContainerResources.ODOMETER.getResource();
        provider.addResource(res1);
        Container container = Containers.generateNew(provider)
                .withResourceNames(res1.getName())
                .withConsentRequired(false);

        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithGeneralAccess(container);

        new GetAllResourcesByVehicleCall(provider.getName(), Vehicle.validVehicleId)
                .withQueryParam("additional-fields=resource&additional-values=odometer")
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedJsonContains("[0].name", "odometer", "Not expected res vale!")
                .expectedJsonContains("$.size()", "1", "Not expected res value!");
    }


    @Test
    @DisplayName("Verify init get single resource by vehicle Id Empty response Async")
    void verifyGetResourceDataRetrievedAsyncFastEmpty() {
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
                .withQueryParam("additional-fields=empty&additional-values=on")
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT)
                .expectedBody(StringUtils.EMPTY, "Expected empty body!");
    }
}
