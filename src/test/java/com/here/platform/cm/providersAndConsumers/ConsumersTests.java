package com.here.platform.cm.providersAndConsumers;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.ConsumerController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.rest.model.Consumer;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.OnBoardConsumer;
import com.here.platform.common.annotations.ErrorHandler;
import com.here.platform.common.annotations.Sentry;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@OnBoardConsumer
@DisplayName("Onboard Consumer")
class ConsumersTests extends BaseCMTest {

    private final ConsumerController consumerController = new ConsumerController();
    private final Consumer testConsumer = new Consumer().consumerId(crypto.md5()).consumerName(crypto.md5());

    @Test
    @Tag("smoke_cm")
    @DisplayName("Verify success on-boarding of a Data Consumer")
    void onboardDataConsumer() {
        var actualOnboardResponse = consumerController
                .withConsumerToken()
                .onboardDataConsumer(testConsumer);
        new ResponseAssertion(actualOnboardResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEmpty();

        var getConsumerResponse = consumerController.getConsumerById(testConsumer.getConsumerId());
        new ResponseAssertion(getConsumerResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(testConsumer);

        RemoveEntitiesSteps.removeConsumer(testConsumer.getConsumerId());

        getConsumerResponse = consumerController.getConsumerById(testConsumer.getConsumerId());
        new ResponseAssertion(getConsumerResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.CONSUMER_NOT_FOUND);
    }

    @Test
    @ErrorHandler
    @DisplayName("Is not possible to onboard Data Consumer without name")
    void onboardConsumerErrorHandlerTest() {
        var actualOnboardResponse = consumerController
                .withConsumerToken()
                .onboardDataConsumer(testConsumer.consumerName(null));

        new ResponseAssertion(actualOnboardResponse).expectedErrorResponse(CMErrorResponse.CONSUMER_VALIDATION);
        testConsumer.consumerId(null);
    }

    @Test
    @Sentry
    @DisplayName("Possible to onboard consumer with CM Application token")
    void possibleToOnboardConsumerWithApplicationToken() {
        var actualOnboardResponse = consumerController
                .withCMToken()
                .onboardDataConsumer(testConsumer);

        new ResponseAssertion(actualOnboardResponse).statusCodeIsEqualTo(StatusCode.OK);
    }

    @Test
    @Sentry
    @DisplayName("Is not possible to onboard consumer without Authorization token")
    void isNotPossibleToOnboardConsumerWithInvalidAuthorizationToken() {
        var actualOnboardResponse = consumerController
                .onboardDataConsumer(testConsumer);

        new ResponseAssertion(actualOnboardResponse).statusCodeIsEqualTo(StatusCode.UNAUTHORIZED);
    }

    @Test
    @Sentry
    @DisplayName("Is not possible to onboard consumer with empty Authorization token")
    void isNotPossibleToOnboardConsumerWithInvalidAuthorizatinToken() {
        var actualOnboardResponse = consumerController
                .withAuthorizationValue("")
                .onboardDataConsumer(testConsumer);

        new ResponseAssertion(actualOnboardResponse).statusCodeIsEqualTo(StatusCode.UNAUTHORIZED);
    }

}
