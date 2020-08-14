package com.here.platform.ns.common;

import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.instruments.CleanUp;
import com.here.platform.ns.restEndPoints.external.ConsentManagementCall;
import com.here.platform.ns.restEndPoints.external.MarketplaceManageListingCall;
import java.io.File;


public class DataCleanUpNS {

    public static void main(String[] args) {
        String env = System.getProperty("env");
        if (!"prod".equalsIgnoreCase(env)) {
            new MarketplaceManageListingCall().providerCleanUp();
            new CleanUp().deleteAllArtificialPoliciesBrute();
            new ConsentManagementCall().deleteAllAutomatedConsentsHard();
        }
        new CleanUp().deleteAllTestProvidersAndContainers();
        new CleanUp().deleteAllTestContainersForProvider(Providers.DAIMLER_REFERENCE.getProvider());
        new CleanUp().deleteAllTestContainersForProvider(Providers.DAIMLER_EXPERIMENTAL.getProvider());
        new CleanUp().deleteAllTestContainersForProvider(Providers.REFERENCE_PROVIDER.getProvider());
        new CleanUp().deleteAllTestContainersForProvider(Providers.DAIMLER_REFERENCE.getProvider());
        File tokens = new File("build/tmp/tokens.txt");
        if (tokens.exists()) {
            tokens.delete();
        }

    }

}
