package com.here.platform.dataProviders.reference.controllers;

import static com.here.platform.common.strings.SBB.sbb;
import static io.restassured.RestAssured.given;

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
                .basePath(targetPath)
                .filters(new AllureRestAssured());

        return baseService;
    }

    @Step
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
                .body(new ReferenceToken(token, refreshToken, 0, vinNumber, scope))
                .post("/tokens");
    }

    @Step
    public Response addApp(String appId, String appSecret) {
        return referenceProviderClient("/admin")
                .header("Content-Type", "application/json")
                .body(new ReferenceApplication(appId, appSecret))
                .post("/apps");
    }

    @Step
    public Response addContainer(Container container) {
        return referenceProviderClient("/admin")
                .header("Content-Type", "application/json")
                .body(new ReferenceContainer(
                        container.getId(),
                        container.getConsentRequired(),
                        Arrays.stream(container.getResourceNames().split(","))
                                .map(item -> "\"" + item + "\"").collect(Collectors.joining(",\n"))))
                .post("/containers");
    }

    @Step
    public Response readSyncEntity(String key) {
        return referenceProviderClient("/sync")
                .get("/entity/" + key);
    }

    @Step
    public Response readServerTime() {
        return referenceProviderClient("/sync")
                .get("/now");
    }

    @Step
    public Response writeSyncEntity(String key, String value, long expirationTime) {
        return referenceProviderClient("/sync")
                .param("key", key)
                .param("value", value)
                .param("expiresIn", expirationTime)
                .post("/entity");
    }

    @Step
    public Response unlockSyncEtity(String key) {
        return referenceProviderClient("/sync")
                .post("/unlock/{key}", key);
    }

    @Step
    public Response lockSyncEtity(String key) {
        return referenceProviderClient("/sync")
                .post("/lock/{key}", key);
    }

    @Data
    @AllArgsConstructor
    public static class ReferenceToken {

        @JsonProperty("tokenId")
        private String tokenId;

        @JsonProperty("refreshTokenId")
        private String refreshTokenId;

        @JsonProperty("expiresIn")
        private int expiresIn;

        @JsonProperty("vin")
        private String vin;

        @JsonProperty("scope")
        private String scope;

    }

    @Data
    @AllArgsConstructor
    public static class ReferenceContainer {

        @JsonProperty("name")
        private String name;

        @JsonProperty("consentRequired")
        private boolean consentRequired;

        @JsonProperty("resourceNames")
        private String resourceNames;

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
