package com.here.platform.cm.enums;

/**
 * Provider application that must be on-boarded by default
 */
//todo extend as builder to simplify onboarding and removing for tests
public enum ProviderApplications {

    DAIMLER_CONS_1(MPConsumers.OLP_CONS_1, DaimlerContainers.DAIMLER_EXPERIMENTAL_CHARGE);

    public final MPProviders provider;
    public final MPConsumers consumer;
    public DaimlerContainers container;

    ProviderApplications(MPConsumers consumer, DaimlerContainers container) {
        this.provider = container.provider;
        this.consumer = consumer;
        this.container = container;
    }

}
