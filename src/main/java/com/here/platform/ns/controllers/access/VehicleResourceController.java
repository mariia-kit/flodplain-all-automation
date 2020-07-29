package com.here.platform.ns.controllers.access;

import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.utils.NS_Config;
import io.qameta.allure.Step;
import io.restassured.response.Response;


public class VehicleResourceController extends BaseNeutralServerAccessController<VehicleResourceController> {

    private final String serviceBasePath = NS_Config.SERVICE_ACCESS.toString();

    @Step
    public Response getVehicleResource(DataProvider provider, String vehicleId, ProviderResource resource) {
        return neutralServerAccessClient(serviceBasePath)
                .get("providers/{providerId}/vehicles/{vehicleId}/resources/{resourceId}", provider.generateHrn(), vehicleId, resource.getName());
    }

    @Step
    public Response getAllVehicleResources(DataProvider provider, String vehicleId) {
        return neutralServerAccessClient(serviceBasePath)
                .get("providers/{providerId}/vehicles/{vehicleId}/resources", provider.generateHrn(), vehicleId);
    }

}
