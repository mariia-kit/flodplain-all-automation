package com.here.platform.ns.dto;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.common.config.Conf;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@EqualsAndHashCode
@ToString
public class DataProvider {

    private String id;
    private String name;
    private String url;
    @EqualsAndHashCode.Exclude
    private List<ProviderResource> resources;

    @ConstructorProperties({"name", "url"})
    public DataProvider(String name, String url) {
        this.id = name;
        this.name = name;
        this.url = url;
        this.resources = new ArrayList<>();
    }

    public Map<String, String> generateBody() {
        if (this.getUrl() != null) {
            return Map.of("url", this.getUrl());
        } else {
            return Map.of();
        }
    }

    public void addResource(ProviderResource resource) {
        resources.add(resource);
    }

    public void addResource(ContainerResources resource) {
        resources.add(resource.getResource());
    }

    public DataProvider clone() {
        return new DataProvider(name, url);
    }

    public DataProvider withName(String name) {
        this.name = name;
        return this;
    }

    public DataProvider withUrl(String url) {
        this.url = url;
        return this;
    }

    public DataProvider withResources(ProviderResource resource) {
        this.addResource(resource);
        return this;
    }

    public DataProvider addResources(List<ContainerResources> resources) {
        this.resources.addAll(resources.stream().map(ContainerResources::getResource).collect(Collectors.toList()));
        return this;
    }

    public String generateHrn() {
        return "hrn:" + Conf.ns().getRealm() + ":neutral::" + PROVIDER.getUser().getRealm() + ":" + name;
    }

}
