package com.here.platform.cm.enums;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum MPProviders {

    DAIMLER_EXPERIMENTAL(18), DAIMLER(17), EXCELSIOR(17); //todo separate name and id for provider

    public int vinLength;

    public String getName() {
        return name().toLowerCase();
    }

}
