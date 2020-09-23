package com.here.platform.cm.consentRequests;

import static org.assertj.core.api.Assertions.assertThat;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.CreateConsentRequest;
import com.here.platform.common.annotations.ErrorHandler;
import com.here.platform.common.annotations.Sentry;
import io.qameta.allure.TmsLink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


@DisplayName("Create consent request")
@CreateConsentRequest
class CreateConsentRequestErrorsTests extends BaseCMTest {

    private final ConsentRequestContainers testContainer = ConsentRequestContainers.generateNew();
    private final ConsentRequestData testConsentRequest = new ConsentRequestData()
            .consumerId(crypto.sha1())
            .providerId(crypto.sha1())
            .title(faker.gameOfThrones().quote())
            .purpose(faker.commerce().productName())
            .privacyPolicy(faker.internet().url())
            .containerId(testContainer.id);

    @Test
    @ErrorHandler
    @DisplayName("Verify It Is Not Possible To Create ConsentRequest With Out Consumer")
    void isNotPossibleToCreateConsentRequestWithOutConsumer() {
        consentRequestController.withConsumerToken();
        final var actualResponse = consentRequestController.createConsentRequest(testConsentRequest);

        var actualCause = new ResponseAssertion(actualResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.CONSUMER_NOT_FOUND)
                .getCause();
        assertThat(actualCause)
                .isEqualTo("Couldn't find consumer by consumer id: " + testConsentRequest.getConsumerId());
    }

    @Test
    @Sentry
    @DisplayName("Verify Sentry Block ConsentRequest Creation")
    void sentryBlockConsentRequestCreationTest() {
        final var actualResponse = consentRequestController.createConsentRequest(testConsentRequest);

        new ResponseAssertion(actualResponse).statusCodeIsEqualTo(StatusCode.UNAUTHORIZED);
    }

    @Test
    @Sentry
    @DisplayName("Is  not possible to create consent request with application token")
    void isNotPossibleToCreateConsentRequestWithApplicationToken() {
        final var actualResponse = consentRequestController
                .withCMToken()
                .createConsentRequest(testConsentRequest);

        new ResponseAssertion(actualResponse).statusCodeIsEqualTo(StatusCode.FORBIDDEN);
    }

    @Test
    @ErrorHandler
    @DisplayName("Verify Create Empty ConsentRequest is forbidden")
    @TmsLink("NS-1350")
    void createEmptyConsentRequestTest() {
        consentRequestController.withConsumerToken();
        final var actualCreateConsentRequestResponse = consentRequestController
                .createConsentRequest(new ConsentRequestData());

        new ResponseAssertion(actualCreateConsentRequestResponse)
                .statusCodeIsEqualTo(StatusCode.BAD_REQUEST)
                .expectedErrorResponse(CMErrorResponse.CONSENT_REQUEST_VALIDATION);
    }

    @Nested
    @DisplayName("Create consent request")
    public class WithConsumer {

        @AfterEach
        void cleanUp() {
            RemoveEntitiesSteps.removeConsumer(testConsentRequest.getConsumerId());
        }

        @Test
        @ErrorHandler
        @DisplayName("Verify It Is Not Possible To Create ConsentRequest With Out Provider")
        void isNotPossibleToCreateConsentRequestWithOutProvider() {
            new OnboardingSteps(null, testConsentRequest.getConsumerId()).onboardValidConsumer();

            consentRequestController.withConsumerToken();
            final var actualResponse = consentRequestController.createConsentRequest(testConsentRequest);

            var actualCause = new ResponseAssertion(actualResponse)
                    .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                    .expectedErrorResponse(CMErrorResponse.PROVIDER_APPLICATION_NOT_FOUND)
                    .getCause();
            assertThat(actualCause)
                    .startsWith("Couldn't find provider by id: ProviderApplicationPK")
                    .contains(testConsentRequest.getConsumerId())
                    .contains(testConsentRequest.getProviderId())
                    .contains(testConsentRequest.getContainerId());
        }

    }

}
