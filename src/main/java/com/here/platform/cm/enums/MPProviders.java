package com.here.platform.cm.enums;

import com.here.platform.ns.dto.Providers;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum MPProviders {

    DAIMLER_EXPERIMENTAL("daimler_experimental", 18),
    DAIMLER("daimler", 17),
    EXCELSIOR(Providers.REFERENCE_PROVIDER.getName(), 17),
    BMW_TEST("test-bmw", 17),
    BMW("bmw", 17);

    private final String name;
    public int vinLength;

    public String getName() {
        return name;
    }

}
