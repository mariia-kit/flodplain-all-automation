package com.here.platform.cm.controllers;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.MPProviders;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.List;


public class PrivateController extends BaseConsentService<PrivateController> {

    private final List<String>
            forbiddenToRemoveProviders = stream(MPProviders.values()).map(MPProviders::getName).collect(toList()),
            forbiddenToRemoveConsumers = stream(MPConsumers.values()).map(MPConsumers::getRealm).collect(toList());


    public static String getPrivateBearer() {
        return "Bearer temp";
    }

    private RequestSpecification basePrivateController() {
        return consentServiceClient("private");
    }

    @Step
    public Response hardDeleteConsentRequest(final String consentRequestId) {
        return basePrivateController()
                .delete("/consentRequest/{consentRequestId}", consentRequestId);
    }

    @Step
    public Response deleteProviderApplication(
            final String providerId,
            final String consumerId,
            final String container
    ) {
        if (isForbiddenToRemoveProvider(providerId) && isForbiddenToRemoveConsumer(consumerId)) {
            return null;
        }
        return basePrivateController()
                .queryParams("providerId", providerId, "consumerId", consumerId, "container", container)
                .delete("/providerApplication");
    }

    @Step
    public Response deleteConsumer(final String consumerId) {
        if (isForbiddenToRemoveConsumer(consumerId)) {
            return null;
        }
        return basePrivateController().delete("/consumer/{consumerId}", consumerId);
    }

    @Step
    public Response deleteProvider(final String providerId) {
        if (isForbiddenToRemoveProvider(providerId)) {
            return null;
        }
        return basePrivateController().delete("/provider/{providerId}", providerId);
    }

    private boolean isForbiddenToRemoveProvider(String providerId) {
        return forbiddenToRemoveProviders.contains(providerId.toLowerCase());
    }

    private boolean isForbiddenToRemoveConsumer(String consumerId) {
        return consumerId == null || forbiddenToRemoveConsumers.contains(consumerId.toLowerCase());
    }


}
