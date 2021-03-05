package com.here.platform.proxy.dto;

public class ProxyErrorList {

    public static ProxyError getDeleteConflictError(Long providerId) {
        return new ProxyError(
                403,
                "Error accessing extsvc provider",
                "E504017",
                "Provider " + providerId + " is in use and cannot be modified",
                "Ensure that provider is not in use/locked and try this operation again",
                "extsvc_provider_is_in_use");
    }
}
