package com.here.platform.ns.access.vehicleResources;

import static com.here.platform.ns.dto.Users.CONSUMER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.access.VehicleResourceAsyncController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.ContainerResources;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.ConsentAfterCleanUp;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.ns.restEndPoints.external.AaaCall;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@DisplayName("Get vehicle resources by Vehicle ID Async")
@ExtendWith({MarketAfterCleanUp.class, ConsentAfterCleanUp.class})
public class GetResourceByVehicleAsyncTest extends BaseNSTest {

    @Test
    @DisplayName("Verify get resources Async by vehicle ID No-PII")
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

        var asyncInit = new VehicleResourceAsyncController()
                .withToken(CONSUMER)
                .initGetVehicleResouce(provider, Vehicle.validVehicleId, res1);
        String location = new NeutralServerResponseAssertion(asyncInit)
                .expectedCode(HttpStatus.SC_ACCEPTED)
                .expectedHeaderIsPresent("Location")
                .getResponse().getHeader("Location");

        var asyncGet = new VehicleResourceAsyncController()
                .withToken(CONSUMER)
                .getVehicleResourceResult(location);
        new NeutralServerResponseAssertion(asyncGet)
                .expectedCode(HttpStatus.SC_OK)
                .expectedJsonContains("resourceReadout.asyncStatus", "InProgress", "Not expected async State!");
        delay(5500L);
        var asyncGet2 = new VehicleResourceAsyncController()
                .withToken(CONSUMER)
                .getVehicleResourceResult(location);
        new NeutralServerResponseAssertion(asyncGet2)
                .expectedCode(HttpStatus.SC_OK)
                .expectedJsonContains("resourceReadout.asyncStatus", "Complete", "Not expected async State!")
                .expectedJsonContains("resourceReadout.resources[0].stateofcharge.value", "0.8",
                        "Not expected res vale!");
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

        var asyncInit = new VehicleResourceAsyncController()
                .withToken(CONSUMER)
                .initGetVehicleResouce(provider, Vehicle.validVehicleId, res1);
        new NeutralServerResponseAssertion(asyncInit)
                .expectedCode(HttpStatus.SC_CREATED)
                .expectedJsonContains("resourceReadout.asyncStatus", "Complete", "Not expected async State!")
                .expectedJsonContains("resourceReadout.resources[0].rangeliquid.value", "1648",
                        "Not expected res value!");
    }

    @Test
    @DisplayName("Verify get resources Async by vehicle ID 404 Error Expected")
    void verifyGetContainersDataRetrievedReferenceFastLAsyncError() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        ProviderResource res1 = new ProviderResource("alastor");
        provider.addResource(res1);
        Container container = Containers.generateNew(provider)
                .withConsentRequired(false);

        Steps.createRegularProvider(provider);
        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithRes(container, res1);

        var asyncInit = new VehicleResourceAsyncController()
                .withToken(CONSUMER)
                .initGetVehicleResouce(provider, Vehicle.validVehicleId, res1);
        new NeutralServerResponseAssertion(asyncInit)
                .expectedCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Verify get resources Async by vehicle ID bad request id Expected")
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

        var asyncInit = new VehicleResourceAsyncController()
                .withToken(CONSUMER)
                .initGetVehicleResouce(provider, Vehicle.validVehicleId, res1);
        String location = new NeutralServerResponseAssertion(asyncInit)
                .expectedCode(HttpStatus.SC_ACCEPTED)
                .expectedHeaderIsPresent("Location")
                .getResponse().getHeader("Location");
        var asyncGet = new VehicleResourceAsyncController()
                .withToken(CONSUMER)
                .getVehicleResourceResult(location + "some_bad_request_id");
        new NeutralServerResponseAssertion(asyncGet)
                .expectedCode(HttpStatus.SC_NOT_FOUND);

    }

}
