package com.here.platform.cm.controllers;

import com.here.platform.cm.rest.model.Provider;
import com.here.platform.cm.rest.model.ProviderApplication;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.Map;


public class ProvidersController extends BaseConsentService<ProvidersController> {

    private final String providersBasePath = "/providers";

    @Step
    public Response onboardDataProvider(Provider dataProviderBody) {
        return consentServiceClient(providersBasePath)
                .body(dataProviderBody)
                .put(dataProviderBody.getId());
    }

    @Step
    public Response getProviderById(String providerRealmId) {
        return consentServiceClient(providersBasePath).get(providerRealmId);
    }

    @Step
    public Response getListOfProviders() {
        return consentServiceClient(providersBasePath).get();
    }

    @Step
    public Response redirectToDataProviderByRequestId(String crid, String callbackUrl) {
        return consentServiceClient(providersBasePath)
                .queryParams(Map.of("consentRequestId", crid, "callbackUrl", callbackUrl))
                .redirects().follow(false)
                .get("oauth");
    }

    @Step
    public Response onboardApplication(ProviderApplication providerApplication) {
        return consentServiceClient(providersBasePath).body(providerApplication).post("/applications");
    }

    @Step
    public Response getListOfApplications() {
        return consentServiceClient(providersBasePath).get("/applications");

    }

}
