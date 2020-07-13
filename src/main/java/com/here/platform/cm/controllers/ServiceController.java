package com.here.platform.cm.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.List;
import lombok.Data;


public class ServiceController extends BaseConsentService {

    @Step("Perform GET call to the /health endpoint")
    public Response health() {
        return consentServiceClient("health")
                .get();
    }

    @Step("Perform GET call to the /healthDeep endpoint")
    public Response deepHealth() {
        return consentServiceClient("healthDeep")
                .get();
    }

    public Response nsClearCache() {
        return consentServiceClient("/cache/ns/clear")
                .delete();
    }

    @Step("Perform GET call to the /version endpoint")
    public Response version() {
        return consentServiceClient("version")
                .get();
    }

    @Data
    public static class ConsentManagementHealth {

        @JsonProperty("serviceBuildNumber")
        private String serviceBuildNumber;

        @JsonProperty("serviceVersion")
        private String serviceVersion;

        @JsonProperty("serviceHealths")
        private List<ServiceHealthsItem> serviceHealths;

        @JsonProperty("health")
        private Boolean health;

        @JsonProperty("serviceName")
        private String serviceName;

        @JsonProperty("timestamp")
        private Long timestamp;

        @Data
        public static class PeersItem {

            @JsonProperty("name")
            private String name;

            @JsonProperty("status")
            private Boolean status;

        }

        @Data
        public static class ServiceHealthsItem {

            @JsonProperty("name")
            private String name;

            @JsonProperty("version")
            private String version;

            @JsonProperty("isHealthy")
            private Boolean health;

            @JsonProperty("peers")
            private List<PeersItem> peers;

            @JsonProperty("compatibleChaincodesVersion")
            private List<String> compatibleChaincodesVersion;

            @JsonProperty("orderers")
            private List<OrderersItem> orderers;

        }

        @Data
        public static class OrderersItem {

            @JsonProperty("name")
            private String name;

            @JsonProperty("status")
            private Boolean status;

        }

    }

}
