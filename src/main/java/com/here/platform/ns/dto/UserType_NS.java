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
    APP("app"),
    CMCONS("cmcons"),
    PROXY_ADM("proxy_admin"),
    PROXY_APP("proxy_app");

    private final String prefix;
}
