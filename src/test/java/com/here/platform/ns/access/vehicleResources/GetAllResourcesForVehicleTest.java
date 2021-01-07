package com.here.platform.ns.access.vehicleResources;


import static com.here.platform.ns.dto.Users.CONSUMER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.access.VehicleResourceController;
import com.here.platform.ns.controllers.provider.ResourceController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.ns.restEndPoints.external.AaaCall;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag("NS-Data provider")
@DisplayName("Get all provider's resources by vehicle Id")
@ExtendWith({MarketAfterCleanUp.class})
@Tag("ignored")
public class GetAllResourcesForVehicleTest extends BaseNSTest {

    @Test
    @DisplayName("Verify get all Container's Resources by Vehicle ID for No-PII container")
    void verifyAllResourceRetrievedReference() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        ProviderResource res1 = new ProviderResource("electricvehicle");
        ProviderResource res2 = new ProviderResource("fuelstatus");

        Steps.createRegularProvider(provider);

        var addResource1 = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res1);
        new NeutralServerResponseAssertion(addResource1)
                .expectedCode(HttpStatus.SC_OK);
        var addResource2 = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res2);
        new NeutralServerResponseAssertion(addResource2)
                .expectedCode(HttpStatus.SC_OK);
        Container container = Containers.generateNew(provider)
                .withResourceNames(res2.getName())
                .withConsentRequired(false);

        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithRes(container, res1);

        var getSingle1 = new VehicleResourceController()
                .withToken(CONSUMER)
                .getAllVehicleResources(provider, Vehicle.validRefVehicleId);
        new NeutralServerResponseAssertion(getSingle1)
                .expectedCode(HttpStatus.SC_OK);
    }

}
