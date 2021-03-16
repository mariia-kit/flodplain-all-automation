package com.here.platform.mp.controllers;

import com.here.platform.cm.steps.remove.DataForRemoveCollector;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.VinsToFile.FILE_TYPE;
import com.here.platform.ns.dto.Container;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;


public class MarketplaceTunnelController extends BaseMPController<MarketplaceTunnelController> {

    private final String neutralServer = "/neutralServer";
    private final String consentManager = "/consent";
    private final String externalProxy = "/mktproxy";

    @Step("Perform MP call to gather NS Data Providers info")
    public Response getProvidersList() {
        return mpClient(neutralServer)
                .get("/providers");
    }

    @Step("Get list of containers for provider {providerName} thru MP.")
    public Response getProviderContainers(String providerName) {
        return mpClient(neutralServer)
                .get("/providers/{providerName}/containers", providerName);
    }

    @Step("Get container info thru MP: {containerHrn}")
    @SneakyThrows
    public Response getGetContainerInfo(String containerHrn) {
        URLEncoder.encode(containerHrn, StandardCharsets.UTF_8.toString());
        return mpClient(neutralServer)
                .get("/containers/{containerHrn}", containerHrn);
    }


    @Step("Create new consent request for {subsId} {container.id} thru MP.")
    public Response createConsent(String subsId, Container container) {
        String body = "{\n"
                + "  \"title\": \"" + container.getName() + " request\",\n"
                + "  \"purpose\": \"Test Consent for " + container.getName() + "\",\n"
                + "  \"privacyPolicy\":\"tratata\",\n"
                + "  \"additionalLinks\":["
                + "  {\"title\":\"title1\",\"url\":\"link1\"},\n"
                + "  {\"title\":\"title2\",\"url\":\"link2\"},\n"
                + "  {\"title\":\"title3\",\"url\":\"link3\"},\n"
                + "  {\"title\":\"title4\",\"url\":\"link4\"},\n"
                + "  {\"title\":\"title5\",\"url\":\"link5\"},\n"
                + "  {\"title\":\"title6\",\"url\":\"link6\"},\n"
                + "  {\"title\":\"title7\",\"url\":\"link7\"},\n"
                + "  {\"title\":\"title8\",\"url\":\"link8\"},\n"
                + "  {\"title\":\"title9\",\"url\":\"link9\"}]"
                + "}";
        var response = mpClient(consentManager)
                .body(body)
                .post("/subscriptions/{subsId}/request", subsId);

        if (response.getStatusCode() == StatusCode.CREATED.code) {
            DataForRemoveCollector.addConsent(response.getBody().jsonPath().get("consentRequestId").toString());
        }
        return response;
    }

    @Step("Perform MP call to get CM ConsentRequest data {subsId}.")
    public Response getConsent(String subsId) {
        return mpClient(consentManager)
                .get("/subscriptions/{subsId}/request", subsId);
    }

    @Step("Perform MP call to get CM ConsentRequest Status {subsId}.")
    public Response getConsentStatus(String subsId) {
        return mpClient(consentManager)
                .get("/subscriptions/{subsId}/requestStatus", subsId);
    }

    @Step("Perform MP call to add vin numbers {vins} to ConsentRequest {crid}")
    public Response addVinNumbers(String crid, String subsId, FILE_TYPE fileType, String... vins) {
        File fileWithVins = new VinsToFile(vins).file(fileType);
        DataForRemoveCollector.addVin(crid, vins);
        return mpClient(consentManager)
                .contentType("multipart/form-data")
                .multiPart("vins", fileWithVins, fileType.getContentType())
                .put("/subscriptions/{subsId}/request", subsId);
    }

    @Step("Perform MP call to get list of Proxy service providers")
    public Response getProxyServiceProviders() {
        return mpClient(externalProxy)
                .get("serviceProviders");
    }

}
