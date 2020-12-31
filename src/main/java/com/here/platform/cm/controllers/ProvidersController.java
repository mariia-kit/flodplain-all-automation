package com.here.platform.cm.controllers;

import com.here.platform.cm.rest.model.Provider;
import com.here.platform.cm.rest.model.ProviderApplication;
import com.here.platform.cm.steps.remove.ConsentCollector;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.Map;


public class ProvidersController extends BaseConsentService<ProvidersController> {

    private final String providersBasePath = "/providers";

    @Step("Onboard Data Provider {dataProvider}")
    public Response onboardDataProvider(Provider dataProvider) {
        return consentServiceClient(providersBasePath)
                .body(dataProvider)
                .put(dataProvider.getId());
    }

    @Step("Get Data Provider by ID: '{providerRealmId}'")
    public Response getProviderById(String providerRealmId) {
        return consentServiceClient(providersBasePath).get(providerRealmId);
    }

    @Step("Redirect data subject to data provider OAUTH by consent request ID: '{crid}' and callback URL: '{callbackUrl}'")
    public Response redirectToDataProviderByRequestId(String crid, String callbackUrl) {
        return consentServiceClient(providersBasePath)
                .queryParams(Map.of("consentRequestId", crid, "callbackUrl", callbackUrl))
                .redirects().follow(false)
                .get("oauth");
    }

    @Step("Onboard Provider Application {providerApplication}")
    public Response onboardApplication(ProviderApplication providerApplication) {
        ConsentCollector.addApp(providerApplication);
        return consentServiceClient(providersBasePath).body(providerApplication).post("/applications");
    }

    @Step("Get all onboarded Data Provider Applications")
    public Response getListOfApplications() {
        return consentServiceClient(providersBasePath).get("/applications");
    }

}
