package com.here.platform.cm.steps.remove;

import com.here.platform.cm.rest.model.ProviderApplication;
import com.here.platform.ns.dto.Container;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;


@UtilityClass
public class ConsentCollector {
    private static List<String> cridsToRemove = new ArrayList<>();
    private static List<ConsentVin> cridsVinsToRemove = new ArrayList<>();
    private static List<ProviderApplication> applications = new ArrayList<>();
    private static List<String> consumer = new ArrayList<>();
    private static List<String> provider = new ArrayList<>();

    private static List<Container> containers = new ArrayList<>();

    public void addConsent(String crid) {
        cridsToRemove.add(crid);
    }

    public void addVin(String crid, String... vins) {
        for (String vin: vins) {
            cridsVinsToRemove.add(new ConsentVin(crid, vin));
        };
    }

    public void addApp(ProviderApplication application) {
        applications.add(application);
    }

    public void addConsumer(String consumerId) {
        consumer.add(consumerId);
    }

    public void addProvider(String providerId) {
        provider.add(providerId);
    }

    public void addNSContainer(Container container) {
        containers.add(container);
    }

    public List<String> getAllConsents() {
        return cridsToRemove;
    }

    public List<ConsentVin> getAllConsentsWithVin() {
        return cridsVinsToRemove;
    }

    public List<ProviderApplication> getAllApplications() {
        return applications;
    }

    public static List<String> getConsumer() {
        return consumer;
    }

    public static List<String> getProvider() {
        return provider;
    }

    public static List<Container> getContainers() {
        return containers;
    }
}
