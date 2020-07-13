package com.here.platform.ns.dto;

import com.here.platform.ns.helpers.resthelper.UrlBuilder;
import com.here.platform.ns.utils.NS_Config;
import lombok.Getter;


@Getter
public enum CallsUrl {

    ADD_CONTAINER(NS_Config.URL_PROVIDER, NS_Config.SERVICE_PROVIDER,
            "providers/{providerId}/containers_info/{containerId}"),
    DELETE_CONTAINER(NS_Config.URL_PROVIDER, NS_Config.SERVICE_PROVIDER,
            "providers/{providerId}/containers_info/{containerId}"),
    GET_CONTAINER_LIST(NS_Config.URL_PROVIDER, NS_Config.SERVICE_PROVIDER,
            "providers/{providerId}/containers_info"),
    GET_CONTAINER(NS_Config.URL_PROVIDER, NS_Config.SERVICE_PROVIDER,
            "providers/{providerId}/containers_info/{containerId}"),

    ADD_PROVIDER(NS_Config.URL_PROVIDER, NS_Config.SERVICE_PROVIDER, "providers/{providerId}"),
    GET_PROVIDERS(NS_Config.URL_PROVIDER, NS_Config.SERVICE_PROVIDER, "providers"),
    DELETE_PROVIDER(NS_Config.URL_PROVIDER, NS_Config.SERVICE_PROVIDER, "providers/{providerId}"),

    GET_PROVIDER_RESOURCES(NS_Config.URL_PROVIDER, NS_Config.SERVICE_PROVIDER,
            "providers/{providerId}/resources"),
    PROVIDER_RESOURCE(NS_Config.URL_PROVIDER, NS_Config.SERVICE_PROVIDER,
            "providers/{providerId}/resources/{resourceId}"),

    GET_PROVIDER_METRICS(NS_Config.URL_PROVIDER, NS_Config.SERVICE_PROVIDER, "ta/metrics"),
    GET_PROVIDER_METRICS_STATISTICS(NS_Config.URL_PROVIDER, NS_Config.SERVICE_PROVIDER,
            "ta/metrics/statistics"),

    GET_CONTAINER_RESOURCE_BY_VEHICLE(NS_Config.URL_NS, NS_Config.SERVICE_ACCESS,
            "providers/{providerId}/vehicles/{vehicleId}/containers/{containerId}"),

    GET_ALL_RESOURCES_BY_VEHICLE(NS_Config.URL_NS, NS_Config.SERVICE_ACCESS,
            "providers/{providerId}/vehicles/{vehicleId}/resources"),
    GET_RESOURCE_VALUE_BY_VEHICLE(NS_Config.URL_NS, NS_Config.SERVICE_ACCESS,
            "providers/{providerId}/vehicles/{vehicleId}/resources/{resourceId}"),
    GET_RESOURCE_VALUE_BY_VEHICLE_ASYNC(NS_Config.URL_NS, NS_Config.SERVICE_ACCESS,
            "providers/{providerId}/vehicles/{vehicleId}/{resourceId}"),

    ACCESS_HEALTH_CHECK(NS_Config.URL_NS, NS_Config.SERVICE_ACCESS, "health"),
    ACCESS_VERSION_CHECK(NS_Config.URL_NS, NS_Config.SERVICE_ACCESS, "version"),
    ACCESS_DEEP_HEALTH_CHECK(NS_Config.URL_NS, NS_Config.SERVICE_ACCESS, "healthDeep"),
    MARKETPLACE_HEALTH_CHECK(NS_Config.URL_PROVIDER, NS_Config.SERVICE_PROVIDER, "health"),
    MARKETPLACE_VERSION_CHECK(NS_Config.URL_PROVIDER, NS_Config.SERVICE_PROVIDER, "version"),
    MARKETPLACE_DEEP_HEALTH_CHECK(NS_Config.URL_PROVIDER, NS_Config.SERVICE_PROVIDER, "healthDeep");

    private String host;
    private String serviceUrl;
    private String pattern;

    CallsUrl(NS_Config host, NS_Config serviceUrl, String pattern) {
        this.host = host.toString();
        this.serviceUrl = serviceUrl.toString();
        this.pattern = pattern;
    }

    public UrlBuilder builder() {
        return new UrlBuilder(host + serviceUrl + pattern);
    }

    public UrlBuilder errorUrlBuilder() {
        return new UrlBuilder("/" + serviceUrl + pattern);
    }

}
