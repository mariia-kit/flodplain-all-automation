package com.here.platform.ns.access.vehicleResources;


import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.ContainerResources;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.instruments.ConsentAfterCleanUp;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import com.here.platform.ns.restEndPoints.external.AaaCall;
import com.here.platform.ns.restEndPoints.neutralServer.resources.GetSingleResourceByVehicleCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@DisplayName("Get date for single resources for vehicle")
@ExtendWith({MarketAfterCleanUp.class, ConsentAfterCleanUp.class})
public class GetSingleResourceByVehicleTest extends BaseNSTest {


    @Test
    @DisplayName("Verify single ContainerResources by vehicle Id no-PII")
    void verifySingleResourceRetrievedReferenceL() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        ProviderResource res1 = ContainerResources.FUEL.getResource();
        provider.addResource(res1);
        Steps.createRegularProvider(provider);

        Container container = Containers.generateNew(provider)
                .withResourceNames(res1.getName())
                .withConsentRequired(false);

        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithRes(container, res1);

        new GetSingleResourceByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                res1.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("Verify single ContainerResources by vehicle Id no-PII empty responce")
    void verifySingleResourceRetrievedReferenceEmpty() {
        DataProvider provider = Providers.REFERENCE_PROVIDER.getProvider();
        ProviderResource res1 = ContainerResources.FUEL.getResource();
        provider.addResource(res1);
        Steps.createRegularProvider(provider);

        Container container = Containers.generateNew(provider)
                .withResourceNames(res1.getName())
                .withConsentRequired(false);

        Steps.createRegularContainer(container);

        new AaaCall().createContainerPolicyWithRes(container, res1);

        new GetSingleResourceByVehicleCall(provider.getName(), Vehicle.validVehicleId,
                res1.getName())
                .withQueryParam("additional-fields=empty&additional-values=on")
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT)
                .expectedBody(StringUtils.EMPTY, "Expected empty body!");
    }

}