package com.here.platform.ns.instruments;

import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.EnvDataCollector;
import com.here.platform.ns.restEndPoints.external.ConsentManagementCall;
import com.here.platform.ns.restEndPoints.external.MarketplaceManageListingCall;
import com.here.platform.ns.restEndPoints.external.ReferenceProviderCall;
import com.here.platform.ns.utils.NS_Config;
import java.io.File;


public class Main {

    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].contains("sit")) {
                System.setProperty("env", "sit");
            }
            if (args[0].contains("prod")) {
                System.setProperty("env", "prod");
            }
            if (args[0].contains("+subs")) {
                String env = System.getProperty("env");
                if (!"prod".equalsIgnoreCase(env)) {
                    new TestDataGeneration().createAllRequiredSubscription();
                }
            }
            if (args[0].contains("+base")) {
                new TestDataGeneration().createBaseContainersIfNecessary();
            }
            if (args[0].contains("+clean")) {
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
                File tokens = new File("tokens.txt");
                if (tokens.exists()) {
                    tokens.delete();
                }
            }
            if (args[0].contains("+vehicle")) {
                new TestDataGeneration().setVehicleTokenForDaimler();
            }
            if (args[0].contains("+init")) {
                ReferenceProviderCall.wipeAllData();
                setAllTokens();
                EnvDataCollector.create();
                if (NS_Config.CONSENT_MOCK.toString().equalsIgnoreCase("true")) {
                    new TestDataGeneration().setVehicleTokenForDaimler();
                }
            }
            if (args[0].contains("+policy")) {
                new TestDataGeneration().createPoliciesForProviderGroup();
            }

        } else {
            System.out.println("Available arguments:");
            System.out.println("+subs");
            System.out.println("+clean");
            System.out.println("+base");
            System.out.println("+init");
            System.out.println("+vehicle");
        }
    }

    public static void setAllTokens() {
        Users.PROVIDER.getUser();
        Users.CONSUMER.getUser();
        Users.DAIMLER.getUser();
        String env = System.getProperty("env");
        if (!"prod".equalsIgnoreCase(env)) {
            Users.AAA.getUser();
            Users.HERE_USER.getUser();
        }
    }

}
