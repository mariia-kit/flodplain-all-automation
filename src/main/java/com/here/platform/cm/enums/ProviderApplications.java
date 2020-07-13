package com.here.platform.cm.enums;

/**
 * Provider application that must be on-boarded by default
 */
public enum ProviderApplications {

    DAIMLER_CONS_1(MPConsumers.OLP_CONS_1, ConsentRequestContainers.CONNECTED_VEHICLE);

    public final MPProviders provider;
    public final MPConsumers consumer;
    public ConsentRequestContainers container;

    ProviderApplications(MPConsumers consumer, ConsentRequestContainers container) {
        this.provider = container.provider;
        this.consumer = consumer;
        this.container = container;
    }

}
