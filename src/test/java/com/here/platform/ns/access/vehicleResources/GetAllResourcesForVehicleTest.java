package com.here.platform.ns.access.vehicleResources;


import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.ConsentAfterCleanUp;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.external.AaaCall;
import com.here.platform.ns.restEndPoints.neutralServer.resources.GetAllResourcesByVehicleCall;
import com.here.platform.ns.restEndPoints.provider.resources.AddProviderResourceCall;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@DisplayName("Get all provider resources by vehicle Id")
@ExtendWith({MarketAfterCleanUp.class, ConsentAfterCleanUp.class})
@Tag("ignored")
public class GetAllResourcesForVehicleTest extends BaseNSTest {

    @Test
    @DisplayName("Verify All ContainerResources by vehicle Id no-PII")
    void verifyAllResourceRetrievedReference() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        ProviderResource res1 = new ProviderResource("electricvehicle");
        ProviderResource res2 = new ProviderResource("fuelstatus");

        Steps.createRegularProvider(provider);

        new AddProviderResourceCall(provider, res1.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);
        new AddProviderResourceCall(provider, res2.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);
        Container container = Containers.generateNew(provider)
                .withResourceNames(res2.getName())
                .withConsentRequired(false);

        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithRes(container, res1);

        new GetAllResourcesByVehicleCall(provider.getName(), Vehicle.validRefVehicleId)
                .call()
                .expectedCode(HttpStatus.SC_OK);
    }

}
