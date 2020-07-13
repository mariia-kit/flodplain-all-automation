package com.here.platform.ns.instruments;

import com.here.platform.ns.helpers.CleanUpHelper;
import com.here.platform.ns.restEndPoints.external.ConsentManagementCall;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


public class ConsentAfterCleanUp implements AfterAllCallback {

    private final static Logger logger = Logger.getLogger(ConsentAfterCleanUp.class);

    public void afterAll(ExtensionContext context) {
        logger.info("Clean up after CM test start!");

        CleanUpHelper.getConsentVinsList().forEach(vin -> new ConsentManagementCall().deleteVinsHard(vin.getCrid(), vin.getVin()));
        CleanUpHelper.getConsentAppsList().forEach(app -> new ConsentManagementCall().deleteApplicationHard(app.getProviderId(), app.getContainerId()));
        CleanUpHelper.getConsentIdsList().forEach(consent -> new ConsentManagementCall().deleteConsentHard(consent));

        CleanUpHelper.getConsentVinsList().clear();
        CleanUpHelper.getConsentAppsList().clear();
        CleanUpHelper.getConsentIdsList().clear();
    }

}
