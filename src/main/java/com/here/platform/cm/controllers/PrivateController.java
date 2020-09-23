package com.here.platform.cm.controllers;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.ProviderApplication;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.List;


public class PrivateController extends BaseConsentService<PrivateController> {

    private final List<String>
            forbiddenToRemoveProviders = stream(MPProviders.values()).map(MPProviders::getName).collect(toList()),
            forbiddenToRemoveConsumers = stream(MPConsumers.values()).map(MPConsumers::getRealm).collect(toList());


    private RequestSpecification basePrivateController() {
        return consentServiceClient("private");
    }

    @Step("Delete data provider application for "
            + "containerId: '{providerApplication.container}', "
            + "providerId: '{providerApplication.providerId}, "
            + "consumerId: '{providerApplication.consumerId}'")
    public Response deleteProviderApplication(ProviderApplication providerApplication) {
        if (isForbiddenToRemoveProviderApplication(providerApplication)) {
            return null;
        }
        return basePrivateController()
                .params(
                        "providerId", providerApplication.getProviderId(),
                        "consumerId", providerApplication.getConsumerId(),
                        "container", providerApplication.getContainer()
                )
                .delete("/providerApplication");
    }

    @Step("Delete data consumer by ID: {consumerId}")
    public Response deleteConsumer(final String consumerId) {
        if (isForbiddenToRemoveConsumer(consumerId)) {
            return null;
        }
        return basePrivateController().delete("/consumer/{consumerId}", consumerId);
    }

    @Step("Delete data provider by ID: {providerId}")
    public Response deleteProvider(final String providerId) {
        if (isForbiddenToRemoveProvider(providerId)) {
            return null;
        }
        return basePrivateController().delete("/provider/{providerId}", providerId);
    }

    @Step("Delete consent request by ID: {consentRequestId}")
    public Response hardDeleteConsentRequest(final String consentRequestId) {
        return basePrivateController()
                .delete("/consentRequest/{consentRequestId}", consentRequestId);
    }

    public Response cleanUpEnvConsents() {
        withCMToken();
        return basePrivateController()
                .delete("/consentRequests");
    }

    private boolean isForbiddenToRemoveProvider(String providerId) {
        return this.forbiddenToRemoveProviders.contains(providerId.toLowerCase());
    }

    private boolean isForbiddenToRemoveConsumer(String consumerId) {
        return consumerId == null || this.forbiddenToRemoveConsumers.contains(consumerId.toLowerCase());
    }

    private boolean isForbiddenToRemoveProviderApplication(ProviderApplication providerApplication) {
        var forbiddenApplications = stream(ConsentRequestContainers.values());

        return providerApplication == null || forbiddenApplications.anyMatch(
                containers -> containers.getId().equals(providerApplication.getContainer())
                        && containers.getProvider().getName().equals(providerApplication.getProviderId()));
    }

}
