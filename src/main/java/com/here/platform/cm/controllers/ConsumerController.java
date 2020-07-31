package com.here.platform.cm.controllers;

import com.here.platform.cm.rest.model.Consumer;
import io.qameta.allure.Step;
import io.restassured.response.Response;


public class ConsumerController extends BaseConsentService<ConsumerController> {

    private final String consumersBasePath = "/consumers";

    @Step
    public Response onboardConsumer(Consumer consumerRequestBody) {
        return consentServiceClient(consumersBasePath)
                .body(consumerRequestBody)
                .put(consumerRequestBody.getConsumerId());

    }

    @Step
    public Response getConsumerById(String consumerId) {
        return consentServiceClient(consumersBasePath)
                .get(consumerId);
    }

}
