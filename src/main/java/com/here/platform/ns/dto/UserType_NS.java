package com.here.platform.ns.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum UserType_NS {

    NS("ns"),
    MP("mp"),
    CM("cm"),
    AA("aa"),
    DAIMLER("dm"),
    APP("app");

    private String prefix;
}
