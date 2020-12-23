package com.here.platform.cm.enums;

import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import lombok.Getter;


/**
 * Provider application that must be on-boarded by default
 */
//todo extend as builder to simplify onboarding and removing for tests
@Getter
public enum ProviderApplications {

    DAIMLER_CONS_1(Users.MP_CONSUMER.getUser(), ConsentRequestContainers.getNextDaimlerExperimental()),
    BMW_CONS_1(Users.MP_CONSUMER.getUser(), ConsentRequestContainers.BMW_MILEAGE),
    REFERENCE_CONS_1(Users.MP_CONSUMER.getUser(), ConsentRequestContainers.DAIMLER_REFERENCE);

    public final MPProviders provider;
    public final User consumer;
    public ConsentRequestContainers container;

    ProviderApplications(User consumer, ConsentRequestContainers container) {
        this.provider = container.provider;
        this.consumer = consumer;
        this.container = container;
    }

}
