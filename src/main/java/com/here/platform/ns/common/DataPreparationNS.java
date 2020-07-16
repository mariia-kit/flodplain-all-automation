package com.here.platform.ns.common;

import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.EnvDataCollector;
import com.here.platform.ns.instruments.TestDataGeneration;
import com.here.platform.ns.restEndPoints.external.ReferenceProviderCall;
import com.here.platform.ns.utils.NS_Config;

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
        if (NS_Config.CONSENT_MOCK.toString().equalsIgnoreCase("true")) {
            new TestDataGeneration().setVehicleTokenForDaimler();
        }
    }
}
