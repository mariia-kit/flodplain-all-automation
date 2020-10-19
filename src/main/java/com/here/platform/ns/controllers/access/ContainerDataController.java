package com.here.platform.ns.controllers.access;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import io.qameta.allure.Step;
import io.restassured.response.Response;


public class ContainerDataController extends BaseNeutralServerAccessController<ContainerDataController> {

    private final String serviceBasePath = Conf.ns().getNsUrlAccess();

    public Response getContainerForVehicle(DataProvider provider, String vehicleId, Container container) {
        return getContainerForVehicle(provider.generateHrn(), vehicleId, container.getId());
    }

    @Step
    public Response getContainerForVehicle(String providerHrn, String vehicleId, String containerId) {
        return neutralServerAccessClient(serviceBasePath)
                .get(
                        "providers/{providerHrn}/vehicles/{vehicleId}/containers/{containerId}",
                        providerHrn,
                        vehicleId,
                        containerId
                );
    }

}
