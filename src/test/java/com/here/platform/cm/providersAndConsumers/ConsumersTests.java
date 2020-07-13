package com.here.platform.cm.providersAndConsumers;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.annotations.CMFeatures.OnBoardConsumer;
import com.here.platform.common.annotations.ErrorHandler;
import com.here.platform.cm.controllers.ConsumerController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.rest.model.Consumer;
import com.here.platform.cm.steps.RemoveEntitiesSteps;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("On-board data consumer")
@OnBoardConsumer
@Tag("smoke_cm")
class ConsumersTests extends BaseCMTest {

    private final ConsumerController consumerController = new ConsumerController();
    private String consumerId;

    @AfterEach
    void afterEach() {
        RemoveEntitiesSteps.removeConsumer(consumerId);
    }

    @Test
    @DisplayName("Verify onboard of Consumer")
    void createConsumerTest() {
        final var testConsumer = new Consumer()
                .consumerId(faker.crypto().sha1())
                .consumerName(faker.commerce().department());

        consumerId = testConsumer.getConsumerId();

        var actualOnboardResponse = consumerController.onboardConsumer(testConsumer);
        new ResponseAssertion(actualOnboardResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEmpty();

        var actualGetConsumerResponse = consumerController.getConsumerById(testConsumer.getConsumerId());
        new ResponseAssertion(actualGetConsumerResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(testConsumer);
    }

    @Test
    @ErrorHandler
    @DisplayName("Verify error during onboard of Consumer")
    void onboardConsumerErrorHandlerTest() {
        var actualOnboardResponse = consumerController.onboardConsumer(new Consumer().consumerId("1"));

        new ResponseAssertion(actualOnboardResponse).expectedErrorResponse(CMErrorResponse.CONSUMER_VALIDATION);
    }

}
