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
    @JsonProperty("serviceName")
    String serviceName;
    @JsonProperty("providerRealm")
    String providerRealm;
    @JsonProperty("domain")
    String domain;
    @JsonProperty("authMethod")
    CredentialsAuthMethod authMethod;

    @JsonProperty("apiKey")
    String apiKey;
    @JsonProperty("apiKeyQueryParamName")
    String apiKeyQueryParamName;
    @JsonProperty("apiKeyHeaderName")
    String apiKeyHeaderName;
    @JsonProperty("authUsername")
    String authUsername;
    @JsonProperty("authPassword")
    String authPassword;

    String scbeId;
    List<ProxyProviderResource> resources = new ArrayList<>();

    public ProxyProvider() {

    };

    public ProxyProvider(String serviceName, String providerRealm, String domain, CredentialsAuthMethod authMethod) {
        this.serviceName = serviceName;
        this.providerRealm = providerRealm;
        this.domain = domain;
        this.authMethod = authMethod;
    }

    public ProxyProvider withAuthMethod(CredentialsAuthMethod authMethod, String key, String value) {
        this.authMethod = authMethod;
        switch (authMethod) {
            case NONE: return this;
            case API_KEY_IN_QUERY:
                setApiKeyQueryParamName(key);
                setApiKey(value);
                return this;
            case API_KEY_IN_HEADER:
                setApiKeyHeaderName(key);
                setApiKey(value);
                return this;
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

    public enum CredentialsAuthMethod {
        NONE,
        API_KEY_IN_QUERY,
        API_KEY_IN_HEADER,
        BASIC_AUTH
    }

}
