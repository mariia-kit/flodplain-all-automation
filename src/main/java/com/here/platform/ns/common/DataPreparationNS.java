package com.here.platform.ns.common;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.EnvDataCollector;
import com.here.platform.ns.instruments.TestDataGeneration;
import com.here.platform.ns.restEndPoints.external.ReferenceProviderCall;


public class DataPreparationNS {

    public static void main(String[] args) {
        ReferenceProviderCall.wipeAllData();

        Users.PROVIDER.getUser();
        Users.CONSUMER.getUser();
        Users.DAIMLER.getUser();
        String env = System.getProperty("env");
        if (!"prod".equalsIgnoreCase(env)) {
            Users.AAA.getUser();
            Users.HERE_USER.getUser();
        }

        EnvDataCollector.create();
        if (Conf.ns().isConsentMock()) {
            new TestDataGeneration().setVehicleTokenForDaimler();
        }
    }

}
