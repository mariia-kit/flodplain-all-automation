package com.here.platform.cm.controllers;

import com.here.platform.cm.rest.model.Consumer;
import io.qameta.allure.Step;
import io.restassured.response.Response;


public class ConsumerController extends BaseConsentService<ConsumerController> {

    private final String consumersBasePath = "/consumers";

    @Step("Onboard data consumer: {consumerBody}")
    public Response onboardDataConsumer(Consumer consumerBody) {
        return consentServiceClient(consumersBasePath)
                .body(consumerBody)
                .put(consumerBody.getConsumerId());

    }

    @Step("Get consumer by id: {consumerId}")
    public Response getConsumerById(String consumerId) {
        return consentServiceClient(consumersBasePath)
                .get(consumerId);
    }

    @Step("Get all consumers")
    public Response getAllConsumers() {
        return consentServiceClient(consumersBasePath)
                .get();
    }

}
