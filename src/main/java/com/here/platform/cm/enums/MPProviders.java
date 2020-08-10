package com.here.platform.cm.enums;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum MPProviders {

    DAIMLER_EXPERIMENTAL("daimler_experimental", 18),
    DAIMLER("daimler", 17),
    EXCELSIOR("exelsior", 17),
    BMW_TEST("test-bmw", 17);

    private String name;
    public int vinLength;

    public String getName() {
        return name;
    }

}
