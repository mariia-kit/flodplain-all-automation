package com.here.platform.ns.common;

import com.here.platform.ns.instruments.CleanUp;
import com.here.platform.mp.steps.api.MarketplaceSteps;
import java.io.File;

public class DataCleanUpNS {

    public static void main(String[] args) {
        String env = System.getProperty("env");
        if (!"prod".equalsIgnoreCase(env)) {
            new MarketplaceSteps().providerCleanUp();
            new CleanUp().deleteAllArtificialPoliciesBrute();
        }

        File tokens = new File("build/tmp/tokens.txt");
        if (tokens.exists()) {
            tokens.delete();
        }
    }

}
