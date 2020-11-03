package com.here.platform.cm.enums;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@AllArgsConstructor
@Data
@Builder
public class ConsentRequestContainer {
    private String
            id,
            name,
            containerDescription,
            scopeValue,
            clientId,
            clientSecret;
    private List<String> resources;
    private MPProviders provider;

    public ConsentRequestContainer withResources(List<String> resources) {
        this.resources = resources;
        return this;
    }
}