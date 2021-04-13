package com.here.platform.ns.access.vehicleResources;


import static com.here.platform.ns.dto.Users.CONSUMER;

import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.access.ContainerDataController;
import com.here.platform.ns.controllers.access.VehicleResourceAsyncController;
import com.here.platform.ns.controllers.access.VehicleResourceController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.ContainerResources;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.ns.restEndPoints.external.AaaCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag("NS-DataProvider")
@DisplayName("Get resources by vehicle Id Pagination, Filtering...")
@ExtendWith({MarketAfterCleanUp.class})
public class GetResourcesNonDaimlerFunc extends BaseNSTest {

    @Test
    @DisplayName("Verify get resources by vehicle Id filter")
    void verifyGetContainersDataRetrieved() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        Container container = Containers.generateNew(provider).withResourceNames("odometer,location");

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);
        Steps.createListingAndSubscription(container);

        ConsentObject consentObj = new ConsentObject(container);
        String crid = new ConsentRequestSteps(consentObj)
                .onboardApplicationForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(Vehicle.validVehicleId)
                .approveConsent(Vehicle.validVehicleId)
                .getId();

        var response1 = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .withQueryParam("resource", "distancesincereset")
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response1)
                .expectedEqualsISOContainerData(
                        Vehicle.getResourceMap(Vehicle.odometerResource, "distancesincereset"),
                        "Container data content not as expected!");

        var response2 = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .withQueryParam("resourceName", "odometer")
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response2)
                .expectedEqualsISOContainerData(
                        Vehicle.odometerResource,
                        "Container data content not as expected!");

        var response3 = new ContainerDataController()
                .withToken(CONSUMER)
                .withConsentId(crid)
                .withQueryParam("resource", "notrealres")
                .getContainerForVehicle(provider, Vehicle.validVehicleId, container);
        new NeutralServerResponseAssertion(response3)
                .expectedEqualsISOContainerData(
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

        var getSingle1 = new VehicleResourceController()
                .withToken(CONSUMER)
                .withQueryParam("resource", "distancesincereset")
                .getVehicleResource(provider, Vehicle.validVehicleId, res1);
        new NeutralServerResponseAssertion(getSingle1)
                .expectedEqualsContainerData(
                        Vehicle.getResourceMap(Vehicle.odometerResource, "distancesincereset"),
                        "Container data content not as expected!");

        var getSingle2 = new VehicleResourceController()
                .withToken(CONSUMER)
                .withQueryParam("resource", "odometer")
                .getVehicleResource(provider, Vehicle.validVehicleId, res1);
        new NeutralServerResponseAssertion(getSingle2)
                .expectedEqualsContainerData(
                        Vehicle.getResourceMap(Vehicle.odometerResource, "odometer"),
                        "Resource data content not as expected!");

        var getSingle3 = new VehicleResourceController()
                .withToken(CONSUMER)
                .withQueryParam("resource", "notrealres")
                .getVehicleResource(provider, Vehicle.validVehicleId, res1);
        new NeutralServerResponseAssertion(getSingle3)
                .expectedEqualsContainerData(Vehicle.empty, "Resource data content not as expected!");
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
        var asyncInit = new VehicleResourceAsyncController()
                .withToken(CONSUMER)
                .withQueryParam("resource", "rangeliquid")
                .initGetVehicleResouce(provider, Vehicle.validVehicleId, res1);
        new NeutralServerResponseAssertion(asyncInit)
                .expectedJsonContains("resourceReadout.resources[0].rangeliquid.value", "1648",
                        "Not expected res value!");

        var asyncInit2 = new VehicleResourceAsyncController()
                .withToken(CONSUMER)
                .withQueryParam("resource", "notrealres")
                .initGetVehicleResouce(provider, Vehicle.validVehicleId, res1);
        new NeutralServerResponseAssertion(asyncInit2)
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
        var asyncInit = new VehicleResourceAsyncController()
                .withToken(CONSUMER)
                .withQueryParam("resource", "rangeliquid")
                .initGetVehicleResouce(provider, Vehicle.validVehicleId, res1);
        String location = new NeutralServerResponseAssertion(asyncInit)
                .expectedCode(HttpStatus.SC_ACCEPTED)
                .expectedHeaderIsPresent("Location")
                .getResponse().getHeader("Location");

        var asyncGet = new VehicleResourceAsyncController()
                .withToken(CONSUMER)
                .withQueryParam("resource", "rangeliquid")
                .getVehicleResourceResult(location);
        new NeutralServerResponseAssertion(asyncGet)
                .expectedCode(HttpStatus.SC_OK)
                .expectedJsonContains("resourceReadout.asyncStatus", "InProgress", "Not expected async State!");
        delay(5500L);
        var asyncGet2 = new VehicleResourceAsyncController()
                .withToken(CONSUMER)
                .withQueryParam("resource", "distancesincereset")
                .getVehicleResourceResult(location);
        new NeutralServerResponseAssertion(asyncGet2)
                .expectedCode(HttpStatus.SC_OK)
                .expectedJsonContains("resourceReadout.asyncStatus", "Complete", "Not expected async State!")
                .expectedJsonContains("resourceReadout.resources[0].distancesincereset.value", "1234",
                        "Not expected res vale!")
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

        var getAll = new VehicleResourceController()
                .withToken(CONSUMER)
                .withQueryParam("resource", "odometer")
                .getAllVehicleResources(provider, Vehicle.validVehicleId);
        new NeutralServerResponseAssertion(getAll)
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

        var asyncInit = new VehicleResourceAsyncController()
                .withToken(CONSUMER)
                .withQueryParam("empty", "on")
                .initGetVehicleResouce(provider, Vehicle.validVehicleId, res1);
        new NeutralServerResponseAssertion(asyncInit)
                .expectedCode(HttpStatus.SC_NO_CONTENT)
                .expectedBody(StringUtils.EMPTY, "Expected empty body!");
    }

}
