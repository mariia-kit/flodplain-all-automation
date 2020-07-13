package com.here.platform.cm.controllers;

import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.rest.model.ConsentRequestData;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.io.File;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;


public class ConsentRequestController extends BaseConsentService {

    private final String consentRequestBasePath = "/consentRequests";
    private String consumerBearerToken = "";

    public ConsentRequestController withConsumerToken(MPConsumers consumer) {
        this.consumerBearerToken = "Bearer " + consumer.getToken();
        return this;
    }

    @Step
    public Response createConsentRequest(ConsentRequestData consentRequestBody) {
        return consentServiceClient(consentRequestBasePath)
                .body(consentRequestBody)
                .post();
    }

    @Step
    public Response getStatusForConsentRequestById(String consentRequestId) {
        return consentServiceClient(consentRequestBasePath)
                .get("/{consentRequestId}/status", consentRequestId);
    }

    @Step
    public Response getAllConsentRequestsByConsumerIdAndVin(String testConsumerId, String testVin) {
        return consentServiceClient(consentRequestBasePath)
                .queryParams(Map.of("consumerId", testConsumerId, "vin", testVin))
                .get();
    }

    @Step
    public Response getConsentRequestById(String consentRequestId) {
        return consentServiceClient(consentRequestBasePath)
                .header("Authorization", this.consumerBearerToken)
                .get(consentRequestId);
    }

    @Step
    @SneakyThrows
    public Response addVinsToConsentRequest(String consentRequestId, File fileWithVins) {
        return consentServiceClient(consentRequestBasePath)
                .contentType("multipart/form-data")
                .multiPart("vins", fileWithVins, getContentTypeByFile(fileWithVins))
                .header("Authorization", consumerBearerToken)
                .put("/{consentRequestId}/addDataSubjects", consentRequestId);
    }

    @Step
    @SneakyThrows
    public Response addVinsToConsentRequestAsync(String consentRequestId, File fileWithVins) {
        return consentServiceClient(consentRequestBasePath)
                .contentType("multipart/form-data")
                .multiPart("vins", fileWithVins, getContentTypeByFile(fileWithVins))
                .header("Authorization", consumerBearerToken)
                .put("/{consentRequestId}/addDataSubjectsAsync", consentRequestId);
    }

    @Step
    @SneakyThrows
    public Response removeVinsFromConsentRequest(String consentRequestId, File fileWithVins) {
        return consentServiceClient(consentRequestBasePath)
                .contentType("multipart/form-data")
                .multiPart("vins", fileWithVins, getContentTypeByFile(fileWithVins))
                .header("Authorization", consumerBearerToken)
                .put("/{consentRequestId}/removeDataSubjectsExceptApproved", consentRequestId);
    }

    @Step
    @SneakyThrows
    public Response removeVinsFromConsentRequestAsync(String consentRequestId, File fileWithVins) {
        return consentServiceClient(consentRequestBasePath)
                .contentType("multipart/form-data")
                .multiPart("vins", fileWithVins, getContentTypeByFile(fileWithVins))
                .header("Authorization", consumerBearerToken)
                .put("/{consentRequestId}/removeNonApprovedVINsAsync", consentRequestId);
    }

    @Step
    @SneakyThrows
    public Response forceRemoveVinsFromConsentRequest(String consentRequestId, File fileWithVins) {
        return consentServiceClient(consentRequestBasePath)
                .contentType("multipart/form-data")
                .multiPart("vins", fileWithVins, getContentTypeByFile(fileWithVins))
                .header("Authorization", consumerBearerToken)
                .put("/{consentRequestId}/removeAllDataSubjects", consentRequestId);
    }

    @Step
    @SneakyThrows
    public Response forceRemoveVinsFromConsentRequestAsync(String consentRequestId, File fileWithVins) {
        return consentServiceClient(consentRequestBasePath)
                .contentType("multipart/form-data")
                .multiPart("vins", fileWithVins, getContentTypeByFile(fileWithVins))
                .header("Authorization", consumerBearerToken)
                .put("/{consentRequestId}/removeAllDataSubjectsAsync", consentRequestId);
    }

    @Step
    public Response getConsentRequestAsyncUpdateInfo(String asyncUpdateInfoId) {
        return consentServiceClient(StringUtils.EMPTY)
                .header("Authorization", consumerBearerToken)
                .get("/consentRequestAsyncUpdateInfo/{asyncUpdateInfoId}", asyncUpdateInfoId);
    }

    @Step
    public Response getConsentRequestPurpose(String consentRequestId, String privateBearerToken) {
        return consentServiceClient(consentRequestBasePath)
                .header("Authorization", privateBearerToken)
                .get("/{consentRequestId}/purpose", consentRequestId);
    }

    private String getContentTypeByFile(File targetFile) {
        if (targetFile.getName().endsWith("json")) {
            return "application/json";
        } else {
            return "text/csv";
        }
    }

}