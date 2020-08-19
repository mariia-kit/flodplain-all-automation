package com.here.platform.ns.controllers.access;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.DataProvider;
import io.qameta.allure.Step;
import io.restassured.response.Response;


public class ContainerDataController extends BaseNeutralServerAccessController<ContainerDataController> {

    private final String serviceBasePath = Conf.ns().getNsUrlAccess();

    @Step
    public Response getContainerForVehicle(DataProvider provider, String vehicleId, Container container) {
        return neutralServerAccessClient(serviceBasePath)
                .get("providers/{providerId}/vehicles/{vehicleId}/containers/{containerId}", provider.generateHrn(), vehicleId, container.getId());
    }

}
