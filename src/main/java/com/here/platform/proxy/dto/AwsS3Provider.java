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
public class AwsS3Provider {
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

        String scbeId;
        List<ProxyProviderResource> resources = new ArrayList<>();

        public AwsS3Provider() {

        };

        public AwsS3Provider(String providerType, String serviceName, String providerRealm, String identifier) {
            this.providerType = providerType;
            this.serviceName = serviceName;
            this.providerRealm = providerRealm;
            this.identifier = identifier;
        }
    public AwsS3Provider(String serviceName, String providerRealm, String identifier) {
        this.serviceName = serviceName;
        this.providerRealm = providerRealm;
        this.identifier = identifier;
    }


        public AwsS3Provider withResource(ProxyProviderResource resource) {
            resources.add(resource);
            return this;
        }

        public AwsS3Provider withId(Long id) {
            this.id = id;
            return this;
        }

    }


