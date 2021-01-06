package com.here.platform.ns.common;

import com.here.platform.common.TestDataGeneration;
import com.here.platform.common.config.Conf;
import com.here.platform.dataProviders.reference.controllers.ReferenceProviderController;
import com.here.platform.ns.helpers.EnvDataCollector;


public class DataPreparationNS {

    public static void main(String[] args) {
        new ReferenceProviderController().cleanUpContainersVehiclesResources();

        EnvDataCollector.create();
        if (Conf.ns().isConsentMock()) {
            TestDataGeneration.setVehicleTokenForDaimler();
        }
    }

}
