package com.here.platform.proxy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class ProxyProvider {
    @JsonProperty("id")
    Long id;
    @JsonProperty("providerType")
    String providerType;
    @JsonProperty("serviceName")
    String serviceName;
    @JsonProperty("providerRealm")
    String providerRealm;
    @JsonProperty("identifier")
    String identifier;
    @JsonProperty("authUsername")
    String authUsername;
    @JsonProperty("authPassword")
    String authPassword;

    @JsonProperty("authentication")
    public Authentication authentication;

    public Authentication getAuthentication() {
        return authentication;
    }
    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public String toString() {
        return "ProxyProvider [id=" + id + ", providerType=" + providerType + ", serviceName=" + serviceName
                +  ", providerRealm=" + providerRealm + ", identifier=" + identifier + ", authentication=" + authentication + "]";
    }


    String scbeId;
    List<ProxyProviderResource> resources = new ArrayList<>();

    public ProxyProvider() {

    };

    public ProxyProvider(String providerType, String serviceName, String providerRealm, String identifier, Authentication authentication) {
        this.providerType = providerType;
        this.serviceName = serviceName;
        this.providerRealm = providerRealm;
        this.identifier = identifier;
        this.authentication = authentication;
    }

    public ProxyProvider(String serviceName, String providerRealm, String identifier, Authentication authenticatio) {
        this.serviceName = serviceName;
        this.providerRealm = providerRealm;
        this.identifier = identifier;
        this.authentication = authentication;
    }

    public ProxyProvider withAuthMethod(AuthMethodPlaceholder authMethodPlaceholder, String key, String value) {
        this.authentication = authentication;
        Authentication authentication1 = new Authentication();
        authentication1.setAuthMethod("NONE");
        authentication1.setAuthMethodPlaceholder("IN_QUERY");
        authentication1.setApiKeyParamName(key);
        authentication1.setApiKeyValue(value);

        switch (authMethodPlaceholder) {
            case NONE: return this;
            case BASIC_AUTH:
                setAuthUsername(key);
                setAuthPassword(getAuthPassword());
                return this;

        }
        return this;
    }


    public ProxyProvider withResource(ProxyProviderResource resource) {
        resources.add(resource);
        return this;
    }

    public ProxyProvider withId(Long id) {
        this.id = id;
        return this;
    }

    public enum AuthMethodPlaceholder {
        IN_QUERY,
        IN_HEADER,
        NONE,
        BASIC_AUTH
    }

}
