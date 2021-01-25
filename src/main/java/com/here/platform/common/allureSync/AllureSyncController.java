package com.here.platform.common.allureSync;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;


public class AllureSyncController {

    protected RequestSpecification allureServiceClient() {
        return given()
                .baseUri("https://reports-api.consent.api.platform.in.here.com")
                .basePath("allure-docker-service")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    public Response uploadReportData(String project_id, String jsonPayload) {
        return allureServiceClient()
                .body(jsonPayload)
                .queryParam("project_id", project_id)
                .queryParam("execution_name", "regression")
                .queryParam("execution_from", "local")
                .queryParam("execution_type", "regression")
                .post("send-results");
    }

    public Response initReportGeneration(String project_id) {
        return allureServiceClient()
                .queryParam("project_id", project_id)
                .queryParam("execution_name", "regression")
                .queryParam("execution_from", "local")
                .queryParam("execution_type", "regression")
                .get("generate-report");
    }

    public Response clearReportData(String project_id) {
        return allureServiceClient()
                .queryParam("project_id", project_id)
                .get("clean-results");
    }
}

