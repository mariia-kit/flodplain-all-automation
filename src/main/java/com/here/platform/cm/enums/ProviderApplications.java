package com.here.platform.cm.enums;

/**
 * Provider application that must be on-boarded by default
 */
//todo extend as builder to simplify onboarding and removing for tests
public enum ProviderApplications {

    DAIMLER_CONS_1(MPConsumers.OLP_CONS_1, ConsentRequestContainers.DAIMLER_EXPERIMENTAL_ODOMETER),
    BMW_CONS_1(MPConsumers.OLP_CONS_1, ConsentRequestContainers.BMW_MILEAGE),
    REFERENCE_CONS_1(MPConsumers.OLP_CONS_1, ConsentRequestContainers.REFERENCE_NEW);

    public final MPProviders provider;
    public final MPConsumers consumer;
    public ConsentRequestContainers container;

    ProviderApplications(MPConsumers consumer, ConsentRequestContainers container) {
        this.provider = container.provider;
        this.consumer = consumer;
        this.container = container;
    }

}
