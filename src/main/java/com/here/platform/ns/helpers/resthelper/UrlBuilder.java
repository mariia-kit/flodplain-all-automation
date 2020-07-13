package com.here.platform.ns.helpers.resthelper;

import com.here.platform.ns.dto.Container;


public class UrlBuilder {

    private static final String containerName = "{containerName}";
    private static final String containerId = "{containerId}";
    private static final String providerName = "{providerName}";
    private static final String providerId = "{providerId}";
    private static final String resourceId = "{resourceId}";
    private static final String vehicleId = "{vehicleId}";
    private String pattern;

    public UrlBuilder(String pattern) {
        this.pattern = pattern;
    }

    public UrlBuilder withContainerName(String name) {
        pattern = pattern.replace(containerName, name);
        pattern = pattern.replace(containerId, name);
        return this;
    }

    public UrlBuilder withProviderName(String name) {
        pattern = pattern.replace(providerName, name);
        pattern = pattern.replace(providerId, name);
        return this;
    }

    public UrlBuilder withResourceId(String resId) {
        pattern = pattern.replace(resourceId, resId);
        return this;
    }

    public UrlBuilder withContainer(Container container) {
        return withContainerName(container.getName())
                .withProviderName(container.getDataProviderName());
    }

    public UrlBuilder withVehicleId(String id) {
        pattern = pattern.replace(vehicleId, id);
        return this;
    }

    public String getUrl() {
        if (pattern.contains("{")) {
            throw new RuntimeException(
                    "Error during calculating service url, not all attributes are set! " + pattern);
        }
        return pattern;
    }

}
