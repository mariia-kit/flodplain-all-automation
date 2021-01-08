package com.here.platform.ns.access.vehicleResources;


import static com.here.platform.ns.dto.Users.CONSUMER;

import com.here.platform.ns.BaseNSTest;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag("NS-DataProvider")
@DisplayName("Get date for single resources for vehicle")
@ExtendWith({MarketAfterCleanUp.class})
public class GetSingleResourceByVehicleTest extends BaseNSTest {

    private DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
    private ProviderResource res1 = ContainerResources.FUEL.getResource();


    @Test
    @DisplayName("Verify get Single Container Resource by vehicle Id no-PII")
    void verifySingleResourceRetrievedReferenceL() {
        provider.addResource(res1);
        Steps.createRegularProvider(provider);

        Container container = Containers.generateNew(provider)
                .withResourceNames(res1.getName())
                .withConsentRequired(false);

        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithRes(container, res1);

        var getSingle1 = new VehicleResourceController()
                .withToken(CONSUMER)
                .getVehicleResource(provider, Vehicle.validVehicleId, res1);
        new NeutralServerResponseAssertion(getSingle1)
                .expectedCode(HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("Verify get single container Resource by vehicle Id no-PII empty response Expected")
    void verifySingleResourceRetrievedReferenceEmpty() {
        provider.addResource(res1);
        Steps.createRegularProvider(provider);

        Container container = Containers.generateNew(provider)
                .withResourceNames(res1.getName())
                .withConsentRequired(false);

        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithRes(container, res1);

        var getSingle1 = new VehicleResourceController()
                .withToken(CONSUMER)
                .withQueryParam("empty", "on")
                .getVehicleResource(provider, Vehicle.validVehicleId, res1);
        new NeutralServerResponseAssertion(getSingle1)
                .expectedCode(HttpStatus.SC_NO_CONTENT)
                .expectedBody(StringUtils.EMPTY, "Expected empty body!");
    }

}
