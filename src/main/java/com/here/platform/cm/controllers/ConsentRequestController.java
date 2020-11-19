package com.here.platform.cm.controllers;

import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.helpers.CleanUpHelper;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.io.File;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;


public class ConsentRequestController extends BaseConsentService<ConsentRequestController> {

    private final String consentRequestBasePath = "/consentRequests";

    @Step("Create consent request with: {consentRequestBody}")
    public Response createConsentRequest(ConsentRequestData consentRequestBody) {
        Response response = consentServiceClient(consentRequestBasePath)
                .body(consentRequestBody)
                .post();
        if (response.getStatusCode() == StatusCode.CREATED.code) {
            CleanUpHelper.getConsentIdsList().add(response.getBody().jsonPath().get("consentRequestId").toString());
        }
        return response;
    }

    @Step("Get Consent Request status by ID: {consentRequestId}")
    public Response getStatusForConsentRequestById(String consentRequestId) {
        return consentServiceClient(consentRequestBasePath)
                .get("/{consentRequestId}/status", consentRequestId);
    }

    @Step("Get Consent Request by ID: {consentRequestId}")
    public Response getConsentRequestById(String consentRequestId) {
        return consentServiceClient(consentRequestBasePath)
                .get(consentRequestId);
    }

    @Step("Add VINs to Consent Request by ID: {consentRequestId}")
    @SneakyThrows
    public Response addVinsToConsentRequest(String consentRequestId, File fileWithVins) {
        return consentServiceClient(consentRequestBasePath)
                .contentType("multipart/form-data")
                .multiPart("vins", fileWithVins, getContentTypeByFile(fileWithVins))
                //.header("Authorization", consumerBearerToken)
                .put("/{consentRequestId}/addDataSubjects", consentRequestId);
    }

    @Step("Asynchronously add VINs to Consent Request by ID: {consentRequestId}")
    @SneakyThrows
    public Response addVinsToConsentRequestAsync(String consentRequestId, File fileWithVins) {
        return consentServiceClient(consentRequestBasePath)
                .contentType("multipart/form-data")
                .multiPart("vins", fileWithVins, getContentTypeByFile(fileWithVins))
                //.header("Authorization", consumerBearerToken)
                .put("/{consentRequestId}/addDataSubjectsAsync", consentRequestId);
    }

    @Step("Remove VINs from Consent Request by ID: {consentRequestId}")
    public Response removeVinsFromConsentRequest(String consentRequestId, File fileWithVins) {
        return consentServiceClient(consentRequestBasePath)
                .contentType("multipart/form-data")
                .multiPart("vins", fileWithVins, getContentTypeByFile(fileWithVins))
                //.header("Authorization", consumerBearerToken)
                .put("/{consentRequestId}/removeDataSubjectsExceptApproved", consentRequestId);
    }

    @Step("ASYNC Remove VINs from consent request for id: '{consentRequestId}'")
    public Response removeVinsFromConsentRequestAsync(String consentRequestId, File fileWithVins) {
        return consentServiceClient(consentRequestBasePath)
                .contentType("multipart/form-data")
                .multiPart("vins", fileWithVins, getContentTypeByFile(fileWithVins))
                //.header("Authorization", consumerBearerToken)
                .put("/{consentRequestId}/removeNonApprovedVINsAsync", consentRequestId);
    }

    @Step("Force remove VINs from consent request for id: '{consentRequestId}'")
    public Response forceRemoveVinsFromConsentRequest(String consentRequestId, File fileWithVins) {
        return consentServiceClient(consentRequestBasePath)
                .contentType("multipart/form-data")
                .multiPart("vins", fileWithVins, getContentTypeByFile(fileWithVins))
                //.header("Authorization", consumerBearerToken)
                .put("/{consentRequestId}/removeAllDataSubjects", consentRequestId);
    }

    @Step("ASYNC Force remove VINs from consent request for id: '{consentRequestId}'")
    public Response forceRemoveVinsFromConsentRequestAsync(String consentRequestId, File fileWithVins) {
        return consentServiceClient(consentRequestBasePath)
                .contentType("multipart/form-data")
                .multiPart("vins", fileWithVins, getContentTypeByFile(fileWithVins))
                //.header("Authorization", consumerBearerToken)
                .put("/{consentRequestId}/removeAllDataSubjectsAsync", consentRequestId);
    }

    @Step("ASYNC Get consent request update info for Update info ID: '{asyncUpdateInfoId}'")
    public Response getConsentRequestAsyncUpdateInfo(String asyncUpdateInfoId) {
        return consentServiceClient(StringUtils.EMPTY)
                //.header("Authorization", consumerBearerToken)
                .get("/consentRequestAsyncUpdateInfo/{asyncUpdateInfoId}", asyncUpdateInfoId);
    }

    @Step("Get consent request purpose for id: '{consentRequestId}'")
    public Response getConsentRequestPurpose(String consentRequestId, String cmBearerToken) {
        return consentServiceClient(consentRequestBasePath)
                .header("Authorization", cmBearerToken)
                .get("/{consentRequestId}/purpose", consentRequestId);
    }

    @Step("Get consent request purpose for consumer: '{consumerId}', container: '{containerId}'")
    public Response getConsentRequestPurpose(String consumerId, String containerId, String cmBearerToken) {
        return consentServiceClient(consentRequestBasePath)
                .header("Authorization", cmBearerToken)
                .queryParams(
                        "consumerId", consumerId,
                        "containerId", containerId
                )
                .get("/purpose");
    }

    @Step
    public Response deleteConsentRequest(final String consentRequestId) {
        return consentServiceClient(consentRequestBasePath)
                .delete(consentRequestId);
    }

    private String getContentTypeByFile(File targetFile) {
        if (targetFile.getName().endsWith("json")) {
            return "application/json";
        } else {
            return "text/csv";
        }
    }

}
