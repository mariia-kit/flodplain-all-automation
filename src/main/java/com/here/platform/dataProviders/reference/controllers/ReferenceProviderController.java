package com.here.platform.dataProviders.reference.controllers;

import static com.here.platform.common.strings.SBB.sbb;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Container;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;


public class ReferenceProviderController {

    protected RequestSpecification referenceProviderClient(final String targetPath) {
        var baseService = given()
                .baseUri(Conf.ns().getRefProviderUrl())
                .basePath(targetPath);

        return baseService;
    }

    @Step("Get clearance ID by VIN and container ID from Reference provider")
    public Response getClearanceByVinAndContainerId(String vin, String bmwContainerName) {
        return referenceProviderClient("/admin")
                .get("/clearance/container/{containerId}/vehicles/{vehicleId}", bmwContainerName, vin);
    }

    @Step
    public Response setClearanceByVinAndContainerId(String clearanceId, String containerId, String vin) {
        return referenceProviderClient("/admin")
                .body(sbb("{").n()
                        .dQuote("clearanceId").append(":").dQuote(clearanceId).append(",").n()
                        .dQuote("containerId").append(":").append(containerId).append(",").n()
                        .dQuote("vin").append(":").dQuote(vin).n()
                        .append("}"))
                .post("/clearance/container");
    }

    @Step
    public Response getAllConsents() {
        return referenceProviderClient("/admin")
                .get("/consents");
    }

    @Step
    public Response cleanUpContainersVehiclesResources() {
        return referenceProviderClient("/admin")
                .get("/wipe");
    }

    @Step
    public Response addToken(String vinNumber, String token, String refreshToken, String scope) {
        return referenceProviderClient("/admin")
                .header("Content-Type", "application/json")
                .body(new ReferenceToken(null, token, refreshToken, 0, 0, vinNumber, scope))
                .post("/tokens");
    }

    @Step
    public Response addToken(ReferenceToken token) {
        return referenceProviderClient("/admin")
                .header("Content-Type", "application/json")
                .body(token)
                .post("/tokens");
    }

    @Step
    public Response getTokens() {
        return referenceProviderClient("/admin")
                .header("Content-Type", "application/json")
                .get("/tokens");
    }

    @Step
    public Response addApp(String appId, String appSecret) {
        return referenceProviderClient("/admin")
                .header("Content-Type", "application/json")
                .body(new ReferenceApplication(appId, appSecret))
                .post("/apps");
    }

    @Step("Add Reference Provider container {container.id} for {container.dataProviderName}")
    public Response addContainer(Container container) {
        return referenceProviderClient("/admin")
                .header("Content-Type", "application/json")
                .body(new ReferenceContainer(
                        container.getId(),
                        container.getConsentRequired(),
                        container.getResourceNames().split(",")))
                .post("/containers");
    }

    //@Step("Read sync entity {key}")
    public Response readSyncEntity(String key) {
        return referenceProviderClient("/sync")
                .get("/entity/" + key);
    }

    @Step
    public Response readServerTime() {
        return referenceProviderClient("/sync")
                .get("/now");
    }

    @Step("Write sync entity {key} value {value} exp {expirationTime}")
    public Response writeSyncEntity(String key, String value, long expirationTime) {
        return referenceProviderClient("/sync")
                .param("key", key)
                .param("value", value)
                .param("expiresIn", expirationTime)
                .post("/entity");
    }

    //@Step("Sync entity un-lock {key}")
    public Response unlockSyncEtity(String key) {
        return referenceProviderClient("/sync")
                .post("/unlock/{key}", key);
    }

    //@Step("Sync entity lock {key}")
    public Response lockSyncEtity(String key) {
        return referenceProviderClient("/sync")
                .post("/lock/{key}", key);
    }

    public Response deleteSyncEtity(String key) {
        return referenceProviderClient("/sync")
                .delete("/entity/{key}", key);
    }

    public Response getAllEntities() {
        return referenceProviderClient("/sync")
                .get("/entity");
    }

    @Data
    @AllArgsConstructor
    @JsonInclude(Include.NON_NULL)
    public static class ReferenceToken {

        @JsonProperty("id")
        private String id;

        @JsonProperty("tokenId")
        private String tokenId;

        @JsonProperty("refreshTokenId")
        private String refreshTokenId;

        @JsonProperty("expiresIn")
        private int expiresIn;

        @JsonProperty("timestamp")
        private long timestamp;

        @JsonProperty("vin")
        private String vin;

        @JsonProperty("scope")
        private String scope;

        public ReferenceToken() {

        }

    }

    @Data
    @AllArgsConstructor
    public static class ReferenceContainer {

        @JsonProperty("name")
        private String name;

        @JsonProperty("consentRequired")
        private boolean consentRequired;

        @JsonProperty("resourceNames")
        private String[] resourceNames;

    }

    @Data
    @AllArgsConstructor
    public static class ReferenceApplication {

        @JsonProperty("clientId")
        private String clientId;

        @JsonProperty("clientSecret")
        private String clientSecret;

    }

}
