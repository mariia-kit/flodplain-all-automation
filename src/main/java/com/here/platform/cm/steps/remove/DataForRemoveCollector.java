package com.here.platform.cm.steps.remove;

import com.here.platform.cm.rest.model.ProviderApplication;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.DataProvider;
import io.qameta.allure.Allure;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;


@UtilityClass
public class ConsentCollector {
    private static Map<String, List<String>> cridsToRemove = new HashMap<>();
    private static Map<String, List<ConsentVin>> cridsVinsToRemove = new HashMap<>();
    private static Map<String, List<ProviderApplication>> applications = new HashMap<>();
    private static Map<String, List<String>> consumer = new HashMap<>();
    private static Map<String, List<String>> provider = new HashMap<>();
    private static Map<String, List<HereUser>> hereAccounts = new HashMap<>();

    private static Map<String, List<Container>> nsContainers = new HashMap<>();
    private static Map<String, List<DataProvider>> nsProviders = new HashMap<>();

    public void addConsent(String crid) {
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        if (!cridsToRemove.containsKey(testId)) {
            cridsToRemove.put(testId, new ArrayList<>());
        }
        cridsToRemove.get(testId).add(crid);
    }

    public void addVin(String crid, String... vins) {
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        for (String vin: vins) {
            if (!cridsVinsToRemove.containsKey(testId)) {
                cridsVinsToRemove.put(testId, new ArrayList<>());
            }
            cridsVinsToRemove.get(testId).add(new ConsentVin(crid, vin));
        };
    }

    public void addApp(ProviderApplication application) {
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        if (!applications.containsKey(testId)) {
            applications.put(testId, new ArrayList<>());
        }
        applications.get(testId).add(application);
    }

    public void addConsumer(String consumerId) {
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        if (!consumer.containsKey(testId)) {
            consumer.put(testId, new ArrayList<>());
        }
        consumer.get(testId).add(consumerId);
    }

    public void addProvider(String providerId) {
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        if (!provider.containsKey(testId)) {
            provider.put(testId, new ArrayList<>());
        }
        provider.get(testId).add(providerId);
    }

    public void addNSContainer(Container container) {
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        if (!nsContainers.containsKey(testId)) {
            nsContainers.put(testId, new ArrayList<>());
        }
        nsContainers.get(testId).add(container);
    }

    public void addNSProvider(DataProvider provider) {
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        if (!nsProviders.containsKey(testId)) {
            nsProviders.put(testId, new ArrayList<>());
        }
        nsProviders.get(testId).add(provider);
    }

    public void addHereUser(HereUser hereUser) {
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        if (!hereAccounts.containsKey(testId)) {
            hereAccounts.put(testId, new ArrayList<>());
        }
        hereAccounts.get(testId).add(hereUser);
    }

    public List<String> getAllConsents(String testId) {
        return cridsToRemove.getOrDefault(testId, new ArrayList<>());
    }

    public List<ConsentVin> getAllConsentsWithVin(String testId) {
        return cridsVinsToRemove.getOrDefault(testId, new ArrayList<>());
    }

    public List<ProviderApplication> getAllApplications(String testId) {
        return applications.getOrDefault(testId, new ArrayList<>());
    }

    public static List<String> getConsumer(String testId) {
        return consumer.getOrDefault(testId, new ArrayList<>());
    }

    public static List<String> getProvider(String testId) {
        return provider.getOrDefault(testId, new ArrayList<>());
    }

    public static List<Container> getNsContainers(String testId) {
        return nsContainers.getOrDefault(testId, new ArrayList<>());
    }

    public static List<DataProvider> getNsProviders(String testId) {
        return nsProviders.getOrDefault(testId, new ArrayList<>());
    }

    public static List<HereUser> getHereAccounts(String testId) {
        return hereAccounts.getOrDefault(testId, new ArrayList<>());
    }
}
