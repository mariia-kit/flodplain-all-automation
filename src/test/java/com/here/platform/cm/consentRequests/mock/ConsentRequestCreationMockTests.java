package com.here.platform.cm.consentRequests.mock;

import static com.here.platform.cm.steps.api.OnboardingSteps.onboardApplicationProviderAndConsumer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.common.JConvert;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
public class ConsentRequestCreationMockTests extends BaseCMTest {

    private final ConsentRequestContainers testScope = ConsentRequestContainers.getRandom();
    private final ConsentRequestData testConsentRequest = new ConsentRequestData()
            .consumerId(MPConsumers.OLP_CONS_1.getRealm())
            .providerId(crypto.sha1())
            .title(faker.gameOfThrones().quote())
            .purpose(faker.commerce().productName())
            .privacyPolicy(faker.internet().url())
            .containerId(testScope.id);
    protected MockServerClient mockServerClient;

    @BeforeEach
    void beforeEach() {
        mockServerClient = new MockServerClient("mock-service", 1080);
        mockServerClient.reset();

        onboardApplicationProviderAndConsumer(
                testConsentRequest.getProviderId(),
                testConsentRequest.getConsumerId(),
                testScope
        );
    }


    @Test
    @Tag("local")
    @DisplayName("Create consent request test")
    void createConsentRequestTest() {
        var expectedCrid = new ConsentRequestIdResponse().consentRequestId(crypto.md5());
        var chaincodeCridResponse = new JConvert(expectedCrid).toJson();

        var chaincodePostConsent = request("/api/consent").withMethod("POST");
        mockServerClient.when(chaincodePostConsent)
                .respond(response(chaincodeCridResponse)
                        .withStatusCode(201)
                        .withContentType(MediaType.JSON_UTF_8)
                        .withHeader("Accept", "application/json"));

        consentRequestController.withCMToken();
        var createConsentRequestResponse = consentRequestController.createConsentRequest(testConsentRequest);

        new ResponseAssertion(createConsentRequestResponse)
                .statusCodeIsEqualTo(StatusCode.CREATED)
                .responseIsEqualToObject(expectedCrid);
    }

}
