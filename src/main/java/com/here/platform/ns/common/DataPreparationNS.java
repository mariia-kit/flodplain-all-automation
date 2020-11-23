package com.here.platform.ns.common;

import com.here.platform.common.TestDataGeneration;
import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.EnvDataCollector;
import com.here.platform.ns.restEndPoints.external.ReferenceProviderCall;


public class DataPreparationNS {

    public static void main(String[] args) {
        ReferenceProviderCall.wipeAllData();

        EnvDataCollector.create();
        if (Conf.ns().isConsentMock()) {
            TestDataGeneration.setVehicleTokenForDaimler();
        }
    }

}
